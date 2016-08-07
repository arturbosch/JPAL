package io.gitlab.arturbosch.jpal.nested

import groovy.transform.CompileStatic

/**
 * Thrown by inner classes handler when the given compilation unit is invalid -
 * contains no classes.
 *
 * @author artur
 */
@CompileStatic
class NoClassesException extends RuntimeException {

	NoClassesException() {
		super("Compilation unit is empty!")
	}
}
