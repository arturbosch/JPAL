package io.gitlab.arturbosch.jpal.resolution

import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.body.Parameter
import com.github.javaparser.ast.expr.SimpleName
import groovy.transform.CompileStatic
import io.gitlab.arturbosch.jpal.ast.LocaleVariableHelper
import io.gitlab.arturbosch.jpal.ast.NodeHelper
import io.gitlab.arturbosch.jpal.core.CompilationInfo
import io.gitlab.arturbosch.jpal.resolve.QualifiedType
import io.gitlab.arturbosch.jpal.resolve.Resolver
import io.gitlab.arturbosch.jpal.resolve.symbols.FieldSymbolReference
import io.gitlab.arturbosch.jpal.resolve.symbols.LocaleVariableSymbolReference
import io.gitlab.arturbosch.jpal.resolve.symbols.ParameterSymbolReference
import io.gitlab.arturbosch.jpal.resolve.symbols.SymbolReference
import io.gitlab.arturbosch.jpal.resolve.symbols.VariableSymbolReference

/**
 * @author Artur Bosch
 */
@CompileStatic
final class MethodLevelVariableSymbolSolver implements Solver {

	private Resolver resolver

	MethodLevelVariableSymbolSolver(Resolver resolver) {
		this.resolver = resolver
	}

	@Override
	Optional<? extends SymbolReference> resolve(SimpleName symbol, CompilationInfo info) {
		// The visibility of variables is Locale > Parameter > Field in java
		def maybeMethod = NodeHelper.findDeclaringMethod(symbol)
		if (maybeMethod.isPresent()) {
			// Locale and parameter checks are done within the declared method
			def wasInMethod = resolveSymbolInMethod(symbol, maybeMethod.get(), info)
			if (wasInMethod.isPresent()) {
				return wasInMethod
			}
		}
		return resolveSymbolInFields(symbol, info)
	}

	Optional<FieldSymbolReference> resolveSymbolInFields(SimpleName symbol, CompilationInfo info) {

		def clazz = NodeHelper.findDeclaringClass(symbol).orElse(null)
		if (clazz) {
			def fields = clazz.getFields()
			def maybe = fields.find { it.variables.find { it.name == symbol } }
			if (maybe) {
				def qualifiedType = resolver.getQualifiedType(info.data, maybe.commonType)
				if (qualifiedType != QualifiedType.UNKNOWN) {
					return Optional.of(new FieldSymbolReference(symbol, qualifiedType, maybe))
				}
			}
		}
		return Optional.empty()
	}

	Optional<? extends VariableSymbolReference> resolveSymbolInMethod(SimpleName symbol,
																	  MethodDeclaration method, CompilationInfo info) {
		def locales = LocaleVariableHelper.find(method)
		def maybe = locales.find { it.variables.find { it.name == symbol } }
		if (maybe != null) {
			def qualifiedType = resolver.getQualifiedType(info.data, maybe.commonType)
			if (qualifiedType != QualifiedType.UNKNOWN) {
				return Optional.of(new LocaleVariableSymbolReference(symbol, qualifiedType, maybe))
			}
		}
		return resolveSymbolInParameters(symbol, method, info)
	}

	Optional<ParameterSymbolReference> resolveSymbolInParameters(SimpleName symbol,
																 MethodDeclaration method, CompilationInfo info) {
		def parameters = method.getNodesByType(Parameter.class)
		def maybe = parameters.find { it.name == symbol }
		if (maybe != null) {
			def qualifiedType = resolver.getQualifiedType(info.data, maybe.type)
			if (qualifiedType != QualifiedType.UNKNOWN) {
				return Optional.of(new ParameterSymbolReference(symbol, qualifiedType, maybe))
			}
		}
		return Optional.empty()
	}
}
