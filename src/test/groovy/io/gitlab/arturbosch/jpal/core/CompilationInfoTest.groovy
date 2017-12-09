package io.gitlab.arturbosch.jpal.core

import io.gitlab.arturbosch.jpal.Helper
import io.gitlab.arturbosch.jpal.resolution.QualifiedType
import io.gitlab.arturbosch.jpal.resolution.Resolver
import spock.lang.Specification

/**
 * @author Artur Bosch
 */
class CompilationInfoTest extends Specification {

	CompilationStorage storage

	def setup() {
		storage = JPAL.newInstance(Helper.BASE_PATH)
	}

	def "can set and retrieve processor"() {
		given: "a compilation unit and dumb processor"
		def info = storage.allCompilationInfo.stream()
				.filter { it.qualifiedType.name == Helper.QUALIFIED_TYPE_DUMMY }
				.findFirst().get()
		def processor = new CompilationInfoProcessor<String>() {
			@Override
			String process(CompilationInfo ci, Resolver resolver) {
				return ""
			}
		}
		when: "running the processor"
		info.runProcessor(processor, null)

		then: "we can obtain same instance through a getter with type"
		info.getProcessedObject(String.class) == ""
	}

	def "is within scope"() {
		given: "compilation info of dummy class"
		def info = storage.allCompilationInfo.stream()
				.filter { it.qualifiedType.name == Helper.QUALIFIED_TYPE_DUMMY }
				.findFirst().get()
		when: "testing different types to for being in scope"
		def listInScope = info.isWithinScope(
				new QualifiedType("java.util.List", QualifiedType.TypeToken.JAVA_REFERENCE))
		def stringInScope = info.isWithinScope(
				new QualifiedType("java.lang.String", QualifiedType.TypeToken.JAVA_REFERENCE))
		def unknownInScope = info.isWithinScope(
				new QualifiedType("java.util.Unknown", QualifiedType.TypeToken.UNKNOWN))
		then: "only the used types within Dummy are found"
		listInScope
		stringInScope
		!unknownInScope

		when:
		info.isWithinScope(
				new QualifiedType("Unknown", QualifiedType.TypeToken.UNKNOWN))
		then:
		thrown(IllegalArgumentException.class)
	}

	def "info holds inner classes"() {
		given: "qualified type of a inner class"
		def qualifiedType = new QualifiedType(
				"io.gitlab.arturbosch.jpal.dummies.test.InnerClassesDummy.InnerClass.InnerInnerClass",
				QualifiedType.TypeToken.REFERENCE)
		when: "getting the declaring class of this inner class"
		def info = storage.getCompilationInfo(qualifiedType).get()
		then: "info has all inner classes"
		info.innerClasses.size() == 2
	}
}
