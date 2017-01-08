package io.gitlab.arturbosch.jpal.core

import io.gitlab.arturbosch.jpal.Helper
import spock.lang.Specification

/**
 * @author Artur Bosch
 */
class UpdatableCompilationStorageTest extends Specification {

	def "domain update tests"() {
		given: "an updatable storage"
		def storage = JPAL.updatable(Helper.BASE_PATH)

		when: "adding a new path to the compilation storage"
		def pathToAdd = Helper.BASE_PATH.resolve("test/TestReference.java")
		def updatedCU = storage.updateCompilationInfoWithSamePaths([pathToAdd])[0]

		then: "a new compilation info is added"
		updatedCU.qualifiedType.shortName == "TestReference"

		when: "a file is relocated"
		def pathToRelocate = Helper.BASE_PATH.resolve("test/InnerClassesDummy.java")
		def relocatedCU = storage.updateRelocatedCompilationInfo(pathToAdd, pathToRelocate).get()
		def removedCU = storage.getCompilationInfo(pathToAdd)

		then: "old path is absent and new present"
		relocatedCU.qualifiedType.shortName == "InnerClassesDummy"
		!removedCU.isPresent()
	}
}
