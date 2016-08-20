package io.gitlab.arturbosch.jpal.core

import io.gitlab.arturbosch.jpal.Helper
import io.gitlab.arturbosch.jpal.resolve.QualifiedType
import spock.lang.Specification

/**
 * @author Artur Bosch
 */
class CompilationInfoTest extends Specification {

	def setup() {
		CompilationStorage.create(Helper.BASE_PATH)
		assert CompilationStorage.isInitialized()
	}

	def "is within scope"() {
		given: "compilation info of dummy class"
		def info = CompilationStorage.allCompilationInfo.stream()
				.filter { it.qualifiedType.name == Helper.QUALIFIED_TYPE_DUMMY }
				.findFirst().get()
		when: "testing different types to for being in scope"
		def listInScope = info.isWithinScope(
				new QualifiedType("java.util.List", QualifiedType.TypeToken.JAVA_REFERENCE))
		def stringInScope = info.isWithinScope(
				new QualifiedType("java.util.String", QualifiedType.TypeToken.JAVA_REFERENCE))
		def unknownInScope = info.isWithinScope(
				new QualifiedType("java.util.Unknown", QualifiedType.TypeToken.UNKNOWN))
		info.isWithinScope(
				new QualifiedType("Unknown", QualifiedType.TypeToken.UNKNOWN))
		then: "only the used types within Dummy are found"
		listInScope
		stringInScope
		!unknownInScope
		thrown(IllegalArgumentException.class)
	}
}
