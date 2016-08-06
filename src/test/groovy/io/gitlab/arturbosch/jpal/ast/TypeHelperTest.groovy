package io.gitlab.arturbosch.jpal.ast

import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.FieldDeclaration
import io.gitlab.arturbosch.jpal.Helper
import spock.lang.Specification

import static io.gitlab.arturbosch.jpal.Helper.CYCLE_DUMMY
import static io.gitlab.arturbosch.jpal.Helper.DUMMY

/**
 * @author artur
 */
class TypeHelperTest extends Specification {

	CompilationUnit unit = Helper.compile(DUMMY)
	ClassOrInterfaceDeclaration clazz = Helper.firstClass(unit)
	FieldDeclaration field = Helper.nth(clazz, 0)

	def "get the class type from just a javaparser type"() {
		given: "plain type object"
		def type = field.getType()
		when: "searching for a class type"
		def classType = TypeHelper.getClassOrInterfaceType(type).get()
		then: "it is present"
		classType.name == "List"
	}

	def "qualified type for only class declaration"() {
		given: "a class declaration"
		when: "searching for the qualified type"
		def qualifiedType = TypeHelper.getQualifiedType(clazz).get()
		then: "the qualified type is build upon the package name"
		qualifiedType.name == "io.gitlab.arturbosch.jpal.dummies.Dummy"
	}

	def "qualified type for class declaration and compilation unit"() {
		given: "a class declaration and a compilation unit"
		when: "searching for the qualified type"
		def qualifiedType = TypeHelper.getQualifiedType(clazz, unit)
		then: "the qualified type is build upon the package name"
		qualifiedType.name == "io.gitlab.arturbosch.jpal.dummies.Dummy"
	}

	def "qualified type for class declaration and the package name"() {
		given: "a class declaration and a package name"
		when: "searching for the qualified type"
		def qualifiedType = TypeHelper.getQualifiedType(clazz, unit.package)
		then: "the qualified type is build upon the package name"
		qualifiedType.name == "io.gitlab.arturbosch.jpal.dummies.Dummy"
	}

	def "get qualified types of inner classes too"() {
		given: "a compilation unit with two inner classes"
		def cycle = Helper.compile(CYCLE_DUMMY)
		when: "searching for inner class types"
		def innerClasses = TypeHelper.getQualifiedTypesOfInnerClasses(cycle)
		then: "two are found"
		innerClasses.size() == 2
	}

}
