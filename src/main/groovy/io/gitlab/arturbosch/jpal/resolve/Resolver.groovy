package io.gitlab.arturbosch.jpal.resolve

import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.body.Parameter
import com.github.javaparser.ast.expr.FieldAccessExpr
import com.github.javaparser.ast.expr.MethodCallExpr
import com.github.javaparser.ast.expr.NameExpr
import com.github.javaparser.ast.expr.SimpleName
import com.github.javaparser.ast.expr.ThisExpr
import com.github.javaparser.ast.type.PrimitiveType
import com.github.javaparser.ast.type.Type
import com.github.javaparser.ast.type.VoidType
import groovy.transform.CompileStatic
import io.gitlab.arturbosch.jpal.ast.LocaleVariableHelper
import io.gitlab.arturbosch.jpal.ast.NodeHelper
import io.gitlab.arturbosch.jpal.ast.TypeHelper
import io.gitlab.arturbosch.jpal.core.CompilationStorage
import io.gitlab.arturbosch.jpal.internal.JdkHelper
import io.gitlab.arturbosch.jpal.internal.Printer
import io.gitlab.arturbosch.jpal.internal.Validate
import io.gitlab.arturbosch.jpal.resolve.symbols.FieldSymbolReference
import io.gitlab.arturbosch.jpal.resolve.symbols.LocaleVariableSymbolReference
import io.gitlab.arturbosch.jpal.resolve.symbols.MethodSymbolReference
import io.gitlab.arturbosch.jpal.resolve.symbols.ParameterSymbolReference
import io.gitlab.arturbosch.jpal.resolve.symbols.SymbolReference
import io.gitlab.arturbosch.jpal.resolve.symbols.VariableSymbolReference

/**
 * Provides a static method to resolve the full qualified name of a class type.
 * Information about the imports, the package and assumptions of jdk classes are used
 * to predict the qualified type. Be aware that this approach does not work 100% as
 * star imports are ignored - they are generally considered as a code smell.
 *
 * {@code
 *
 * Usage:
 *
 * 	ClassOrInterfaceType myType = ...
 * 	CompilationUnit unit = ...
 * 	ResolutionData data = ResolutionData.of(unit)
 * 	QualifiedType qualifiedType = Resolver.getQualifiedTypeFromPackage(data, myType)
 *
 *}
 *
 * @author artur
 */
@CompileStatic
final class Resolver {

	private Resolver() {}

	/**
	 * Tries to find the correct qualified name. Considered options are
	 * primitives, boxed primitives, jdk types and reference types within imports or the package.
	 * This approach works on class or interface types as this type is searched
	 * from within the given type.
	 *
	 * @param data resolution data which must be provided from a compilation unit
	 * @param type type of given declaration
	 * @return the qualified type of given type
	 */
	static QualifiedType getQualifiedType(ResolutionData data, Type type) {
		Validate.notNull(data)
		Validate.notNull(type)

		if (type instanceof VoidType) {
			return new QualifiedType("java.lang." + type.toString(Printer.NO_COMMENTS), QualifiedType.TypeToken.JAVA_REFERENCE)
		}

		if (type instanceof PrimitiveType) {
			return new QualifiedType(type.type.name(), QualifiedType.TypeToken.PRIMITIVE)
		}

		def maybeClassOrInterfaceType = TypeHelper.getClassOrInterfaceType(type)
		// TODO Resolve inner classes
		if (maybeClassOrInterfaceType.isPresent()) {
			def realType = maybeClassOrInterfaceType.get()
			if (realType.isBoxedType()) {
				return new QualifiedType("java.lang." + realType.name, QualifiedType.TypeToken.BOXED_PRIMITIVE)
			} else {
				String name = realType.name
				def maybeFromImports = getFromImports(name, data)
				if (maybeFromImports.isPresent()) {
					return maybeFromImports.get()
				} else {
					if (JdkHelper.isPartOfJava(name)) {
						return new QualifiedType("java.lang." + name, QualifiedType.TypeToken.JAVA_REFERENCE)
					}
					// lets assume it is in the same package
					return new QualifiedType("$data.packageName.$name", QualifiedType.TypeToken.REFERENCE)
				}
			}
		}

		return QualifiedType.UNKNOWN
	}

	private static Optional<QualifiedType> getFromImports(String name, ResolutionData data) {
		Validate.notEmpty(name)
		def importName = trimInnerClasses(name)

		def imports = data.imports
		if (imports.keySet().contains(importName)) {
			def qualifiedName = imports.get(importName)
			def qualifiedNameWithInnerClass = qualifiedName.substring(0, qualifiedName.lastIndexOf('.') + 1) + name
			def typeToken = qualifiedName.startsWith("java") ?
					QualifiedType.TypeToken.JAVA_REFERENCE : QualifiedType.TypeToken.REFERENCE
			return Optional.of(new QualifiedType(qualifiedNameWithInnerClass, typeToken))
		} else if (CompilationStorage.isInitialized()) {
			return data.importsWithAsterisk.stream()
					.map { new QualifiedType("$it.$name", QualifiedType.TypeToken.REFERENCE) }
					.filter { CompilationStorage.getCompilationInfo(it).isPresent() }
					.findFirst()

		}
		return Optional.empty()
	}

	private static String trimInnerClasses(String name) {
		name.contains(".") ? name.substring(0, name.indexOf('.')) : name
	}

	static Optional<? extends SymbolReference> resolveSymbol(SimpleName symbol, ResolutionData data) {

		// MethodCalls and FieldAccesses must be handled before searching locally for declarations
		// for this we take a look at the parent as a simple name does not tell us the scope of the symbol

		// first a symbol can be a field access of 'this' or some other symbol
		def maybeFieldAccess = asFieldAccess(symbol)
		if (maybeFieldAccess) {
			if (isThisAccess(maybeFieldAccess)) {
				return resolveSymbolInFields(symbol, data)
			} else { // resolve parent symbol
				return resolveSymbolInFieldsGlobal(maybeFieldAccess, data, symbol)
			}
		}

		// like fields accesses, method calls can be located in 'this' resolution data or
		// have to searched within the compilation storage
		def maybeCallExpr = asMethodCall(symbol)
		if (maybeCallExpr) {
			if (isThisAccess(maybeCallExpr)) {
				// Introduce ResolutionData inside CompilationInfo
				return resolveMethodSymbol(symbol, data, NodeHelper.findDeclaringCompilationUnit(symbol).get(), maybeCallExpr)
			} else { // resolve parent symbol
				return resolveMethodSymbolGlobal(symbol, data, maybeCallExpr)
			}
		}

		// Ok, the symbol is located inside this resolution data
		// The visibility of variables is Locale > Parameter > Field in java
		def maybeMethod = NodeHelper.findDeclaringMethod(symbol)
		if (maybeMethod.isPresent()) {
			// Locale and parameter checks are done within the declared method
			def wasInMethod = resolveSymbolInMethod(symbol, maybeMethod.get(), data)
			if (wasInMethod.isPresent()) {
				return wasInMethod
			}
		}
		return resolveSymbolInFields(symbol, data)
	}

	private static Optional<? extends SymbolReference> resolveSymbolInFieldsGlobal(FieldAccessExpr maybeFieldAccess,
																				   ResolutionData data, SimpleName symbol) {
		def maybeScope = maybeFieldAccess.scope
		def parentSymbol = maybeScope.filter { it instanceof NameExpr }
				.map { it as NameExpr }
				.map { it.name }
				.orElse(null)

		SymbolReference symbolReference = parentSymbol ?
				resolveSymbol(parentSymbol, data).orElse(null) : null
		if (symbolReference && CompilationStorage.initialized) {
			def info = CompilationStorage.getCompilationInfo(symbolReference.qualifiedType).orElse(null)
			return info ? resolveSymbolInFields(symbol, ResolutionData.of(info.unit)) : Optional.empty()
		} else {
			println "Parent symbol resolved, but CS was not initialized"
			return Optional.empty()
		}
	}

	static Optional<FieldSymbolReference> resolveSymbolInFields(SimpleName symbol, ResolutionData data) {

		def clazz = NodeHelper.findDeclaringClass(symbol).orElse(null)
		if (clazz) {
			def fields = clazz.getFields()
			def maybe = fields.find { it.variables.find { it.name == symbol } }
			if (maybe) {
				def qualifiedType = getQualifiedType(data, maybe.commonType)
				if (qualifiedType != QualifiedType.UNKNOWN) {
					return Optional.of(new FieldSymbolReference(symbol, qualifiedType, maybe))
				}
			}
		}
		return Optional.empty()
	}

	static Optional<? extends VariableSymbolReference> resolveSymbolInMethod(SimpleName symbol,
																			 MethodDeclaration method, ResolutionData data) {
		def locales = LocaleVariableHelper.find(method)
		def maybe = locales.find { it.variables.find { it.name == symbol } }
		if (maybe != null) {
			def qualifiedType = getQualifiedType(data, maybe.commonType)
			if (qualifiedType != QualifiedType.UNKNOWN) {
				return Optional.of(new LocaleVariableSymbolReference(symbol, qualifiedType, maybe))
			}
		}
		return resolveSymbolInParameters(symbol, method, data)
	}

	static Optional<ParameterSymbolReference> resolveSymbolInParameters(SimpleName symbol,
																		MethodDeclaration method, ResolutionData data) {
		def parameters = method.getNodesByType(Parameter.class)
		def maybe = parameters.find { it.name == symbol }
		if (maybe != null) {
			def qualifiedType = getQualifiedType(data, maybe.type)
			if (qualifiedType != QualifiedType.UNKNOWN) {
				return Optional.of(new ParameterSymbolReference(symbol, qualifiedType, maybe))
			}
		}
		return Optional.empty()
	}

	private
	static Optional<MethodSymbolReference> resolveMethodSymbol(SimpleName symbol, ResolutionData dataOfArguments,
															   CompilationUnit searchScope, MethodCallExpr methodCall) {
		def methods = searchScope.getNodesByType(MethodDeclaration.class)
		def numberOfArguments = methodCall.arguments.size()
		def maybe = methods.find {
			def numberOfParameters = it.parameters.size()
			// TODO evaluate arguments and parameters
			it.name == symbol && numberOfArguments == numberOfParameters
		}
		if (maybe) {
			def qualifiedType = getQualifiedType(ResolutionData.of(searchScope), maybe.type)
			if (qualifiedType != QualifiedType.UNKNOWN) {
				return Optional.of(new MethodSymbolReference(symbol, qualifiedType, maybe))
			}
		}

		return Optional.empty()
	}

	private static Optional<? extends SymbolReference> resolveMethodSymbolGlobal(SimpleName symbol, ResolutionData data,
																				 MethodCallExpr maybeCallExpr) {
		def parentSymbol = Optional.ofNullable(maybeCallExpr.scope)
				.filter { it instanceof NameExpr }
				.map { it as NameExpr }
				.map { it.name }
				.orElse(null)

		// found declaration of declaring type of he method called
		SymbolReference symbolReference = parentSymbol ?
				resolveSymbol(parentSymbol, data).orElse(null) : null

		if (symbolReference && CompilationStorage.initialized) {
			def info = CompilationStorage.getCompilationInfo(symbolReference.qualifiedType).orElse(null)
			return info ? resolveMethodSymbol(symbol, data, info.unit, maybeCallExpr) : Optional.empty()
		} else {
			// TODO loop through method calls
			println "Parent symbol resolved, but CS was not initialized"
			return Optional.empty()
		}
	}

	private static MethodCallExpr asMethodCall(SimpleName symbol) {
		return symbol.getParentNode()
				.filter { it instanceof MethodCallExpr }
				.map { it as MethodCallExpr }
				.orElse(null)
	}

	private static FieldAccessExpr asFieldAccess(SimpleName symbol) {
		return symbol.getParentNode()
				.filter { it instanceof FieldAccessExpr }
				.map { it as FieldAccessExpr }
				.orElse(null)
	}

	private static boolean isThisAccess(FieldAccessExpr fieldAccessExpr) {
		def scope = fieldAccessExpr.scope
		return !scope.isPresent() || scope.filter { it instanceof ThisExpr }.isPresent()
	}

	private static boolean isThisAccess(MethodCallExpr methodCallExpr) {
		def scope = Optional.ofNullable(methodCallExpr.scope)
		return !scope.isPresent() || scope.filter { it instanceof ThisExpr }.isPresent()
	}

}
