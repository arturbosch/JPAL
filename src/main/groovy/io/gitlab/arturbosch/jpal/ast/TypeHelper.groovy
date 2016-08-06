package io.gitlab.arturbosch.jpal.ast

import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.PackageDeclaration
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.TypeDeclaration
import com.github.javaparser.ast.type.ClassOrInterfaceType
import com.github.javaparser.ast.type.ReferenceType
import com.github.javaparser.ast.type.Type
import io.gitlab.arturbosch.jpal.resolve.QualifiedType
import io.gitlab.arturbosch.jpal.resolve.ResolutionData
import io.gitlab.arturbosch.jpal.resolve.Resolver

/**
 * Provides static methods to search for specific types.
 *
 * @author artur
 */
final class TypeHelper {

	private TypeHelper() {}

	/**
	 * From a given type try to find it's class or interface type.
	 * This is a convenience method as javaparser often returns just the
	 * abstract {@code Type} which is often not helpful.
	 *
	 * @param type given type
	 * @return maybe a class or interface type
	 */
	static Optional<ClassOrInterfaceType> getClassOrInterfaceType(Type type) {
		Type tmp = type
		while (tmp instanceof ReferenceType) {
			tmp = (tmp as ReferenceType).getType()
		}
		if (tmp instanceof ClassOrInterfaceType) {
			return Optional.of(tmp as ClassOrInterfaceType)
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
		def name = n.name
		def holder = new ResolutionData(unit.package, unit.imports)
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
	static QualifiedType getQualifiedType(ClassOrInterfaceDeclaration n, PackageDeclaration packageDeclaration) {
		return new QualifiedType("$packageDeclaration.packageName.$n.name", QualifiedType.TypeToken.REFERENCE)
	}

	/**
	 * Finds the qualified types of all classes within given compilation unit
	 * including the inner classes.
	 *
	 * @param unit compilation unit
	 * @return set of qualified types
	 */
	static Set<QualifiedType> getQualifiedTypesOfInnerClasses(CompilationUnit unit) {
		List<TypeDeclaration> types = unit.getTypes()
		if (types.size() >= 1) {
			TypeDeclaration mainClass = types[0]
			String packageName = unit?.package?.packageName ?: ""
			Set<String> innerClassesNames = NodeHelper.findNamesOfInnerClasses(mainClass)
			String outerClassName = mainClass.name
			return innerClassesNames.collect() {
				new QualifiedType("$packageName.$outerClassName.$it", QualifiedType.TypeToken.REFERENCE)
			}
		} else {
			//TODO
			throw new RuntimeException()
		}
	}

}
