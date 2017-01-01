package io.gitlab.arturbosch.jpal.resolve

import com.github.javaparser.ast.type.ClassOrInterfaceType
import com.github.javaparser.ast.type.PrimitiveType
import com.github.javaparser.ast.type.UnknownType
import io.gitlab.arturbosch.jpal.Helper
import io.gitlab.arturbosch.jpal.core.CompilationStorage
import spock.lang.Specification

/**
 * @author artur
 */
class ResolverTest extends Specification {

	def setup() {
		CompilationStorage.create(Helper.BASE_PATH)
	}

	def "get qualified type from imports"() {
		given: "resolution data for cycle dummy with asterisk imports and initialized compilation storage"
		def data = ResolutionData.of(Helper.compile(Helper.CYCLE_DUMMY))
		when: "retrieving qualified type for two types"
		def helper = Resolver.getQualifiedType(data, new ClassOrInterfaceType("Helper"))
		def testReference = Resolver.getQualifiedType(data, new ClassOrInterfaceType("TestReference"))
		def innerClasses = Resolver.getQualifiedType(data,
				new ClassOrInterfaceType("InnerClassesDummy.InnerClass.InnerInnerClass"))
		then: "Helper type is retrieved from qualified import and TestReference from asterisk"
		helper.name == "io.gitlab.arturbosch.jpal.Helper"
		testReference.name == "io.gitlab.arturbosch.jpal.dummies.test.TestReference"
		innerClasses.name == "io.gitlab.arturbosch.jpal.dummies.test.InnerClassesDummy.InnerClass.InnerInnerClass"
	}

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
		innerCycleType = new ClassOrInterfaceType("CycleDummy.InnerCycleOne")
		javaType = new ClassOrInterfaceType("ArrayList")
		primitiveType = new PrimitiveType(PrimitiveType.Primitive.BOOLEAN)
		boxedType = primitiveType.toBoxedType()
		unknownType = new UnknownType()
	}

}
