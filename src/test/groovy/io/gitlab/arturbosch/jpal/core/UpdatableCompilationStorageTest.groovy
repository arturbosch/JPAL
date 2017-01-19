package io.gitlab.arturbosch.jpal.core

import com.github.javaparser.utils.Pair
import io.gitlab.arturbosch.jpal.Helper
import spock.lang.Specification

import java.nio.file.Files
import java.util.stream.Collectors

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
		def relocatedCU = storage.relocateCompilationInfo([pathToAdd, pathToRelocate].toSpreadMap())[0]
		def removedCU = storage.getCompilationInfo(pathToAdd)

		then: "old path is absent and new present"
		relocatedCU.qualifiedType.shortName == "InnerClassesDummy"
		!removedCU.isPresent()

		when: "removing a path"
		storage.removeCompilationInfo([relocatedCU.path])

		then: "there is no info anymore for this path"
		!storage.getCompilationInfo(relocatedCU.path).isPresent()
	}

	def "updatable from source code domain test"() {
		given: "an updatable storage"
		def storage = JPAL.updatable()

		when: "adding a new path/content pair to the compilation storage"
		def path = Helper.BASE_PATH.resolve("test/TestReference.java")
		def content = Files.lines(path)
				.collect(Collectors.joining("\n"))
		def info = storage.updateCompilationInfo([path, content].toSpreadMap())[0]

		then: "info has right path and class name"
		info.path == path
		info.mainType.nameAsString == "TestReference"

		when: "relocating path with new content"
		content = Files.lines(Helper.DUMMY).collect(Collectors.joining("\n"))
		def relocatedInfo = storage.relocateCompilationInfoFromSource(
				[path, new Pair(Helper.DUMMY, content)].toSpreadMap())[0]

		then: "relocated info's name and path has changed"
		relocatedInfo.path == Helper.DUMMY
		relocatedInfo.mainType.nameAsString == "Dummy"
	}

	def "updatable postprocessing of used types"() {
		expect:
		!info.usedTypes.isEmpty()
		where:
		info = JPAL.updatable().updateCompilationInfo([Helper.RESOLVING_DUMMY])[0]
	}
}
