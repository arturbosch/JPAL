package io.gitlab.arturbosch.jpal.core

import io.gitlab.arturbosch.jpal.Helper
import spock.lang.Specification

/**
 * @author Artur Bosch
 */
class UpdatableCompilationStorageTest extends Specification {

	def "domain update tests"() {
		given: "an updatable storage"
		def storage = JPAL.initializedUpdatable(Helper.BASE_PATH)

		when: "adding a new path to the compilation storage"
		def pathToAdd = Helper.BASE_PATH.resolve("test/TestReference.java")
		def updatedCU = storage.updateCompilationInfo([pathToAdd])[0]

		then: "a new compilation info is added"
		updatedCU.qualifiedType.shortName == "TestReference"

		when: "a file is relocated"
		def pathToRelocate = Helper.BASE_PATH.resolve("test/InnerClassesDummy.java")
		def relocatedCU = storage.relocateCompilationInfo(pathToAdd, pathToRelocate).get()
		def removedCU = storage.getCompilationInfo(pathToAdd)

		then: "old path is absent and new present"
		relocatedCU.qualifiedType.shortName == "InnerClassesDummy"
		!removedCU.isPresent()

		when: "removing a path"
		storage.removeCompilationInfo([relocatedCU.path])

		then: "there is no info anymore for this path"
		!storage.getCompilationInfo(relocatedCU.path).isPresent()
	}
}
