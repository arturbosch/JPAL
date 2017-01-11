package io.gitlab.arturbosch.jpal.resolution

import com.github.javaparser.ast.Node
import com.github.javaparser.ast.expr.Expression
import com.github.javaparser.ast.expr.FieldAccessExpr
import com.github.javaparser.ast.expr.MethodCallExpr
import com.github.javaparser.ast.expr.SimpleName
import com.github.javaparser.ast.nodeTypes.NodeWithOptionalScope
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName
import groovy.transform.CompileStatic
import io.gitlab.arturbosch.jpal.core.CompilationInfo
import io.gitlab.arturbosch.jpal.core.CompilationStorage
import io.gitlab.arturbosch.jpal.resolve.QualifiedType
import io.gitlab.arturbosch.jpal.resolve.Resolver
import io.gitlab.arturbosch.jpal.resolve.symbols.FieldSymbolReference
import io.gitlab.arturbosch.jpal.resolve.symbols.MethodSymbolReference
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
	private ObjectCreationSymbolSolver objectCreationSolver
	private NameLevelSymbolSolver nameLevelSolver

	GlobalClassLevelSymbolSolver(CompilationStorage storage, Resolver resolver,
								 NameLevelSymbolSolver nameLevelSolver,
								 LocalClassLevelSymbolSolver classLevelSolver) {
		this.storage = storage
		this.resolver = resolver
		this.classLevelSolver = classLevelSolver
		this.methodLevelSolver = new MethodLevelVariableSymbolSolver(resolver)
		this.objectCreationSolver = new ObjectCreationSymbolSolver(resolver)
		this.nameLevelSolver = nameLevelSolver
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

	Optional<FieldSymbolReference> resolveFieldSymbolGlobal(SimpleName symbol,
															FieldAccessExpr maybeFieldAccess,
															CompilationInfo info) {
		// is access not chained? then it must belong to a name expr
		SymbolReference symbolReference = nameLevelSolver.resolveNameExprScope(maybeFieldAccess.scope as Optional<Node>, info)
		if (symbolReference) {
			return resolveFieldInTypeScope(symbolReference.qualifiedType, symbol)
		}

		// not chained but within an object creation expression?
		SymbolReference objReference = objectCreationSolver.resolveInFieldAccess(symbol, maybeFieldAccess, info).orElse(null)
		if (objReference) {
			return resolveFieldInTypeScope(objReference.qualifiedType, symbol)
		}

		// loop through field/method accesses
		symbolReference = asMethodOrFieldChain(maybeFieldAccess.scope, info)
		if (symbolReference) {
			return resolveFieldInTypeScope(symbolReference.qualifiedType, symbol)
		}

		return Optional.empty()
	}

	Optional<MethodSymbolReference> resolveMethodSymbolGlobal(SimpleName symbol,
															  MethodCallExpr maybeCallExpr,
															  CompilationInfo info) {
		// not chained but in scope of a name expr? -> we resolve the class and search for this symbol in methods
		SymbolReference symbolReference = nameLevelSolver.resolveNameExprScope(maybeCallExpr.scope as Optional<Node>, info)
		if (symbolReference) {
			return resolveMethodInTypeScope(symbolReference.qualifiedType, symbol, maybeCallExpr)
		}

		// not chained but within an object creation expression?
		symbolReference = objectCreationSolver.resolveInMethodCall(symbol, maybeCallExpr, info).orElse(null)
		if (symbolReference) {
			return resolveMethodInTypeScope(symbolReference.qualifiedType, symbol, maybeCallExpr)
		}

		// loop through field/method accesses
		symbolReference = asMethodOrFieldChain(maybeCallExpr.scope, info)
		if (symbolReference) {
			return resolveMethodInTypeScope(symbolReference.qualifiedType, symbol, maybeCallExpr)
		}

		return Optional.empty()
	}

	private SymbolReference asMethodOrFieldChain(Optional<Expression> scope, CompilationInfo info) {
		scope// Needs #696 (NodeWithOptionalScope) and #702 (NodeWithSimpleName for FieldAccess) - see ticket #21!
				.filter { it instanceof NodeWithOptionalScope }
				.filter { it instanceof NodeWithSimpleName }
				.map { it as NodeWithSimpleName }
				.map { resolve(it.name, info).orElse(null) }
				.orElse(null)
	}

	private Optional resolveFieldInTypeScope(QualifiedType qualifiedType, SimpleName symbol) {
		def otherInfo = storage.getCompilationInfo(qualifiedType).orElse(null)
		return otherInfo ? classLevelSolver.resolveFieldSymbol(symbol, qualifiedType, otherInfo) : Optional.empty()
	}

	private Optional resolveMethodInTypeScope(QualifiedType qualifiedType, SimpleName symbol, MethodCallExpr maybeCallExpr) {
		def otherInfo = storage.getCompilationInfo(qualifiedType).orElse(null)
		// search scope is important if it is in a inner class, call expr for args<->params type comparison
		return otherInfo ? classLevelSolver.resolveMethodSymbol(symbol, maybeCallExpr, qualifiedType, otherInfo) : Optional.empty()
	}

}
