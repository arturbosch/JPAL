package io.gitlab.arturbosch.jpal.internal

import java.util.stream.Stream

/**
 * @author Artur Bosch
 */
final class Optionals {

	private Optionals() {}

	static <T> Stream<T> stream(Optional<T> opt) {
		return opt.map { Stream.of(it) }.orElseGet { Stream.empty() }
	}
}
