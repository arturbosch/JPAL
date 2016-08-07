package io.gitlab.arturbosch.jpal.core

import io.gitlab.arturbosch.jpal.Helper
import io.gitlab.arturbosch.jpal.resolve.QualifiedType
import spock.lang.Specification

/**
 * @author artur
 */
class CompilationTreeTest extends Specification {

	def root = Helper.BASE_PATH

	def "setup"() {
		CompilationTree.registerRoot(root)
	}

	def "cleanup"() {
		CompilationTree.reset()
	}

	def "find compilation unit from a qualified type"() {
		given: "a qualified type of the cycle dummy"
		def qualifiedType = new QualifiedType("io.gitlab.arturbosch.jpal.dummies.CycleDummy",
				QualifiedType.TypeToken.REFERENCE)
		when: "searching for corresponding compilation unit"
		def unit = CompilationTree.findCompilationUnit(qualifiedType)
		then: "compilation unit of the cycle dummy is found"
		unit.isPresent()
	}

	def "find compilation unit from a path"() {
		given: "a path to the cycle dummy"
		def path = Helper.CYCLE_DUMMY
		when: "searching for corresponding compilation unit"
		def unit = CompilationTree.findCompilationUnit(path)
		then: "compilation unit of the cycle dummy is found"
		unit.isPresent()
	}
}
