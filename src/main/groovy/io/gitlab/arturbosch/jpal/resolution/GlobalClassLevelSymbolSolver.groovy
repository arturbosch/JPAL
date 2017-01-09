package io.gitlab.arturbosch.jpal.resolution

import com.github.javaparser.ast.expr.FieldAccessExpr
import com.github.javaparser.ast.expr.MethodCallExpr
import com.github.javaparser.ast.expr.NameExpr
import com.github.javaparser.ast.expr.ObjectCreationExpr
import com.github.javaparser.ast.expr.SimpleName
import groovy.transform.CompileStatic
import io.gitlab.arturbosch.jpal.core.CompilationInfo
import io.gitlab.arturbosch.jpal.core.CompilationStorage
import io.gitlab.arturbosch.jpal.resolve.Resolver
import io.gitlab.arturbosch.jpal.resolve.symbols.ObjectCreationSymbolReference
import io.gitlab.arturbosch.jpal.resolve.symbols.SymbolReference

/**
 * @author Artur Bosch
 */
@CompileStatic
final class GlobalClassLevelSymbolSolver extends CallOrAccessAwareSolver implements Solver {

	private Resolver resolver
	private CompilationStorage storage
	private LocalClassLevelSymbolSolver classLevelSolver
	private MethodLevelVariableSymbolSolver methodLevelSolver

	GlobalClassLevelSymbolSolver(Resolver resolver, CompilationStorage storage) {
		this.storage = storage
		this.resolver = resolver
		this.classLevelSolver = new LocalClassLevelSymbolSolver(resolver)
		this.methodLevelSolver = new MethodLevelVariableSymbolSolver(resolver)
	}

	@Override
	Optional<? extends SymbolReference> resolve(SimpleName symbol, CompilationInfo info) {
		// MethodCalls and FieldAccesses must be handled before searching locally for declarations
		// for this we use the local class level resolver as he will check for 'this' or no scope
		// (eg. this.call() or call() or this.field or field)
		def symbolReference = classLevelSolver.resolve(symbol, info)
		if (symbolReference.isPresent()) return symbolReference

		// Ok, symbol does not belong to a local class, let's search global
		symbolReference = withNotNull(asFieldAccess(symbol)) {
			resolveFieldSymbolGlobal(symbol, it, info)
		}
		if (symbolReference.isPresent()) return symbolReference

		symbolReference = withNotNull(asMethodCall(symbol)) {
			resolveMethodSymbolGlobal(symbol, it, info)
		}
		if (symbolReference.isPresent()) return symbolReference

		return Optional.empty()
	}

	private Optional<? extends SymbolReference> resolveFieldSymbolGlobal(SimpleName symbol,
																		 FieldAccessExpr maybeFieldAccess, CompilationInfo info) {
		// is access not chained? then it must belong to a name expr
		SymbolReference symbolReference = maybeFieldAccess.scope
				.filter { it instanceof NameExpr }
				.map { it as NameExpr }
				.map { it.name }
				.map { methodLevelSolver.resolve(it, info) }
				.filter { it.isPresent() }
				.map { it.get() }
				.orElse(null)

		if (symbolReference) {
			def otherInfo = storage.getCompilationInfo(symbolReference.qualifiedType).orElse(null)
			return otherInfo ? classLevelSolver.resolveFieldSymbol(symbol, symbolReference.qualifiedType, otherInfo) : Optional.empty()
		} else {
			// not chained but within a object creation?
			def objReference = maybeFieldAccess.scope
					.filter { it instanceof ObjectCreationExpr }
					.map { it as ObjectCreationExpr }
					.map {
				new ObjectCreationSymbolReference(symbol, resolver.getQualifiedType(info.data, it.type), it)
			}
			if (objReference.isPresent()) return objReference

			// TODO loop through field/method accesses
			println "$symbol: TODO loop through field/method accesses"
			return Optional.empty()
		}
	}

	private Optional<? extends SymbolReference> resolveMethodSymbolGlobal(SimpleName symbol,
																		  MethodCallExpr maybeCallExpr,
																		  CompilationInfo info) {
		def parentSymbol = maybeCallExpr.scope
				.filter { it instanceof NameExpr }
				.map { it as NameExpr }
				.map { it.name }
				.orElse(null)

		// TODO handle static things
		SymbolReference symbolReference = parentSymbol ?
				methodLevelSolver.resolve(parentSymbol, info).orElse(null) : null

		if (symbolReference) {
			def searchScope = symbolReference.qualifiedType
			def otherInfo = storage.getCompilationInfo(searchScope).orElse(null)
			return otherInfo ? classLevelSolver.resolveMethodSymbol(symbol, maybeCallExpr, searchScope, otherInfo) : Optional.empty()
		} else {
			// TODO loop through field/method accesses
			println "$symbol: TODO loop through field/method accesses"
			return Optional.empty()
		}
	}

}
