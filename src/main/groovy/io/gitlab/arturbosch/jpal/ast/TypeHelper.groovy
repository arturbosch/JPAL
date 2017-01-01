package io.gitlab.arturbosch.jpal.ast

import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.PackageDeclaration
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.TypeDeclaration
import com.github.javaparser.ast.type.ClassOrInterfaceType
import com.github.javaparser.ast.type.Type
import groovy.transform.CompileStatic
import io.gitlab.arturbosch.jpal.internal.Validate
import io.gitlab.arturbosch.jpal.resolve.QualifiedType
import io.gitlab.arturbosch.jpal.resolve.ResolutionData
import io.gitlab.arturbosch.jpal.resolve.Resolver

import java.util.stream.Collectors

/**
 * Provides static methods to search for specific types.
 *
 * @author artur
 */
@CompileStatic
final class TypeHelper {

	public static final String DEFAULT_PACKAGE = "<default>"

	private TypeHelper() {}

	/**
	 * From a given type try to find it's class or interface type.
	 * This is a convenience method as javaparser often returns just the
	 * abstract {@code Type} which is often not helpful.
	 *
	 * {@code
	 *
	 * E.g. you have a FieldDeclaration field = ...
	 * 	Optional<ClassOrInterfaceType> maybeFullType = TypeHelper.getClassOrInterfaceType(field.getType())
	 *
	 *}
	 *
	 * @param type given type
	 * @return maybe a class or interface type
	 */
	static Optional<ClassOrInterfaceType> getClassOrInterfaceType(Type type) {
		if (type instanceof ClassOrInterfaceType) {
			return Optional.of(type as ClassOrInterfaceType)
		}
		return Optional.empty()
	}

	/**
	 * Fastest way to find the qualified type from a class or interface.
	 * As this method makes use of finding the compilation unit first and then
	 * asking the resolver for the type, this method may be really slow.
	 *
	 * Consider caching the compilation unit and use the resolver if performance
	 * is important.
	 *
	 * @param n class or interface declaration
	 * @return maybe the qualified type for given class/interface declaration as the
	 * searched compilation unit can be not found
	 */
	static Optional<QualifiedType> getQualifiedType(ClassOrInterfaceDeclaration n) {
		return NodeHelper.findDeclaringCompilationUnit(n).map { getQualifiedType(n, it) }
	}

	/**
	 * Convenient method to get the qualified type by providing the compilation unit
	 * and the type declaration.
	 * @param n type declaration
	 * @param unit compilation uni
	 * @return a qualified type - be aware it can be unknown
	 */
	static QualifiedType getQualifiedType(ClassOrInterfaceDeclaration n, CompilationUnit unit) {
		def name = n.nameAsString
		def holder = ResolutionData.of(unit)
		return Resolver.getQualifiedType(holder, new ClassOrInterfaceType(name))
	}

	/**
	 * Convenient method to get the qualified type if you are sure the given class is inside the
	 * given package.
	 *
	 * @param n type declaration
	 * @param packageDeclaration package declaration
	 * @return a qualified type consisting of the package name and the type name - be aware it is handled
	 * as a reference type
	 */
	static QualifiedType getQualifiedTypeFromPackage(TypeDeclaration n, Optional<PackageDeclaration> packageDeclaration) {
		return new QualifiedType("${packageDeclaration.map { it.nameAsString }.orElse(DEFAULT_PACKAGE)}.$n.name",
				QualifiedType.TypeToken.REFERENCE)
	}

	/**
	 * Tests if given type is within given compilation unit.
	 *
	 * @param unit the compilation unit
	 * @param qualifiedType searched type
	 * @return true if found
	 */
	static boolean isTypePresentInCompilationUnit(CompilationUnit unit, QualifiedType qualifiedType) {
		Validate.notNull(unit)
		def shortName = Validate.notNull(qualifiedType).shortName()
		def types = unit.getNodesByType(ClassOrInterfaceType.class)
		return types.any { it.nameAsString == shortName }
	}

	/**
	 * Finds the qualified types of all inner classes within given compilation unit.
	 *
	 * @param unit compilation unit
	 * @return set of qualified types
	 */
	static Set<QualifiedType> getQualifiedTypesOfInnerClasses(CompilationUnit unit) {
		List<TypeDeclaration> types = unit.getTypes()
		if (types.size() >= 1) {
			TypeDeclaration mainClass = types[0]
			String packageName = unit.packageDeclaration.map { it.nameAsString }.orElse("")
			Set<String> innerClassesNames = NodeHelper.findNamesOfInnerClasses(mainClass)
			String outerClassName = mainClass.name
			return innerClassesNames.stream().map {
				new QualifiedType("$packageName.$outerClassName.$it", QualifiedType.TypeToken.REFERENCE)
			}.collect(Collectors.toSet())
		} else {
			return Collections.emptySet()
		}
	}

}
