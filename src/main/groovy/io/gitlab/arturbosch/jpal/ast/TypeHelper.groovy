package io.gitlab.arturbosch.jpal.ast

import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.PackageDeclaration
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.body.TypeDeclaration
import com.github.javaparser.ast.type.ClassOrInterfaceType
import com.github.javaparser.ast.type.ReferenceType
import com.github.javaparser.ast.type.Type
import io.gitlab.arturbosch.jpal.resolve.QualifiedType
import io.gitlab.arturbosch.jpal.resolve.ResolutionData
import io.gitlab.arturbosch.jpal.resolve.Resolver

/**
 * @author artur
 */
final class TypeHelper {

	private TypeHelper() {}

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

	static Optional<QualifiedType> getQualifiedType(ClassOrInterfaceDeclaration n) {
		return NodeHelper.findDeclaringCompilationUnit(n).map { getQualifiedType(n, it) }
	}

	static QualifiedType getQualifiedType(ClassOrInterfaceDeclaration n, CompilationUnit unit) {
		def name = n.name
		def holder = new ResolutionData(unit.package, unit.imports)
		return Resolver.getQualifiedType(holder, new ClassOrInterfaceType(name))
	}

	static QualifiedType getQualifiedType(ClassOrInterfaceDeclaration n, PackageDeclaration declaration) {
		return new QualifiedType("$declaration.packageName.$n.name", QualifiedType.TypeToken.REFERENCE)
	}

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
