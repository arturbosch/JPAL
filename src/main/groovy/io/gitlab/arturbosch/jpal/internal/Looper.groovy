package io.gitlab.arturbosch.jpal.internal

import groovy.transform.CompileStatic

/**
 * @author artur
 */
@CompileStatic
class Looper {
	private Closure code

	static Looper loop(Closure code) {
		new Looper(code: code)
	}

	void until(Closure test) {
		code()
		while (!test()) {
			code()
		}
	}
}
