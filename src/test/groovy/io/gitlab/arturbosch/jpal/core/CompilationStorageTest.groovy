package io.gitlab.arturbosch.jpal.core

import io.gitlab.arturbosch.jpal.Helper
import io.gitlab.arturbosch.jpal.resolve.QualifiedType
import spock.lang.Specification

/**
 * @author artur
 */
class CompilationStorageTest extends Specification {

	private QualifiedType cycleType = new QualifiedType("io.gitlab.arturbosch.jpal.CycleDummy",
			QualifiedType.TypeToken.REFERENCE)

	private QualifiedType innerCycleType = new QualifiedType(cycleType.name + ".InnerCycleOne",
			QualifiedType.TypeToken.REFERENCE)

	def "domain tests"() {
		given:
		CompilationStorage.create(Helper.BASE_PATH)

		when: "retrieving all compilation info"
		def info = CompilationStorage.allCompilationInfo

		then: "its size must be greater than 1 as more than 2 dummies are known)"
		info.size() > 1

		when: "retrieving a specific type (cycle)"
		def cycleInfo = CompilationStorage.getCompilationInfo(cycleType).get()

		then: "it should have 2 inner classes"
		cycleInfo.innerClasses.size() == 2

		when: "retrieving info for a inner class"
		def infoFromInnerClass = CompilationStorage.getCompilationInfo(innerCycleType).get()

		then: "it should return info of outer class"
		infoFromInnerClass.qualifiedType == cycleType

	}
}
