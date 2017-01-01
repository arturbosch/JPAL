package io.gitlab.arturbosch.jpal.ast.source

import groovy.transform.CompileStatic
import groovy.transform.Immutable
import groovy.transform.ToString

import java.nio.file.Path

/**
 * Represents a source path.
 *
 * @author artur
 */
@Immutable
@ToString(includePackage = false, includeNames = false)
@CompileStatic
class SourcePath {

	String path

	static SourcePath of(Path path) {
		return new SourcePath(path.toAbsolutePath().normalize().toString())
	}

	@Override
	String toString() {
		return path
	}
}
