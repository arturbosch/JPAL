package io.gitlab.arturbosch.jpal.nested

import groovy.transform.CompileStatic

/**
 * @author artur
 */
@CompileStatic
class NoClassesException extends RuntimeException {

	NoClassesException() {
		super("Compilation unit is empty!")
	}
}
