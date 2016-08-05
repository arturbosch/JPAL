package io.gitlab.arturbosch.jpal.resolve

import com.github.javaparser.ast.type.PrimitiveType
import com.github.javaparser.ast.type.Type
import groovy.transform.CompileStatic
import io.gitlab.arturbosch.jpal.internal.JdkHelper
import io.gitlab.arturbosch.jpal.ast.TypeHelper

/**
 * @author artur
 */
@CompileStatic
final class Resolver {

	private Resolver() {}

	static QualifiedType getQualifiedType(ResolutionData data, Type type) {
		if (type instanceof PrimitiveType) {
			return new QualifiedType(type.type.toString(), QualifiedType.TypeToken.PRIMITIVE)
		}

		def maybeClassOrInterfaceType = TypeHelper.getClassOrInterfaceType(type)

		if (maybeClassOrInterfaceType.isPresent()) {
			def realType = maybeClassOrInterfaceType.get()
			if (realType.isBoxedType()) {
				return new QualifiedType("java.lang." + realType.name, QualifiedType.TypeToken.BOXED_PRIMITIVE)
			} else {
				String name = realType.toString()
				def imports = data.imports
				if (imports.entrySet().contains(name)) {
					String qualifiedName = imports.get(name)
					return new QualifiedType(qualifiedName, QualifiedType.TypeToken.REFERENCE)
				} else {
					if (JdkHelper.isPartOfJava(name)) {
						return new QualifiedType("java.lang." + name, QualifiedType.TypeToken.JAVA_REFERENCE)
					}
					// lets assume it is in the same package
					return new QualifiedType("$data.packageName.$name", QualifiedType.TypeToken.REFERENCE)
				}
			}
		}

		return new QualifiedType("UNKNOWN", QualifiedType.TypeToken.UNKNOWN)
	}

}
