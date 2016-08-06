package io.gitlab.arturbosch.jpal.ast

import io.gitlab.arturbosch.jpal.Helper
import spock.lang.Specification

/**
 * @author artur
 */
class NodeHelperTest extends Specification {

	def "declaring tests"() {
		given:
		def firstMethodNode = Helper.nth(Helper.compile(Helper.CYCLE_DUMMY), 1)

		when:
		def clazz = NodeHelper.findDeclaringClass(firstMethodNode).get()
		def unit = NodeHelper.findDeclaringCompilationUnit(firstMethodNode).get()
		def method = NodeHelper.findDeclaringMethod(firstMethodNode)

		then:
		clazz.name == "CycleDummy"
		unit.types[0].name == "CycleDummy"
		!method.isPresent()
	}

	def "find fields and methods"() {
		given:
		def clazz = Helper.firstClass(Helper.compile(Helper.CYCLE_DUMMY))

		when:
		def fields = NodeHelper.findFields(clazz)
		def privFields = NodeHelper.findPrivateFields(clazz)
		def methods = NodeHelper.findMethods(clazz)
		def privMethods = NodeHelper.findPrivateMethods(clazz)

		then:
		fields.size() == 2
		privFields.isEmpty()
		methods.size() == 2
		privMethods.size() == 1
	}
}
