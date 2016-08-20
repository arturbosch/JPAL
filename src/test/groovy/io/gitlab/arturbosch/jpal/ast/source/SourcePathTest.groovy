package io.gitlab.arturbosch.jpal.ast.source

import spock.lang.Specification

import java.nio.file.Paths

/**
 * @author Artur Bosch
 */
class SourcePathTest extends Specification {

	def "created source path is absolute path"() {
		given:
		def path = SourcePath.of(Paths.get("."))
		when:
		def javaPath = Paths.get(path.path)
		then:
		javaPath.isAbsolute()
	}

}
