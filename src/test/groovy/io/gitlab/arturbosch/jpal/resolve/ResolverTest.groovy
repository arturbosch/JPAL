package io.gitlab.arturbosch.jpal.resolve

import com.github.javaparser.ast.type.ClassOrInterfaceType
import com.github.javaparser.ast.type.PrimitiveType
import com.github.javaparser.ast.type.UnknownType
import io.gitlab.arturbosch.jpal.Helper
import spock.lang.Specification

/**
 * @author artur
 */
class ResolverTest extends Specification {

	def "domain tests"() {
		expect: "the right qualified types"
		Resolver.getQualifiedType(data, importType).isReference()
		Resolver.getQualifiedType(data, cycleType).isReference()
		Resolver.getQualifiedType(data, innerCycleType).isReference()
		Resolver.getQualifiedType(data, javaType).isFromJdk()
		Resolver.getQualifiedType(data, primitiveType).isPrimitive()
		Resolver.getQualifiedType(data, boxedType).isPrimitive()
		Resolver.getQualifiedType(data, unknownType).typeToken == QualifiedType.TypeToken.UNKNOWN

		where: "resolution data from the cycle class and different kinds of class types"
		unit = Helper.compile(Helper.CYCLE_DUMMY)
		data = ResolutionData.of(unit)
		importType = new ClassOrInterfaceType("Helper")
		cycleType = new ClassOrInterfaceType("CycleDummy")
		innerCycleType = new ClassOrInterfaceType("InnerCycleOne")
		javaType = new ClassOrInterfaceType("ArrayList")
		primitiveType = new PrimitiveType(PrimitiveType.Primitive.Boolean)
		boxedType = primitiveType.toBoxedType()
		unknownType = new UnknownType()
	}

}
