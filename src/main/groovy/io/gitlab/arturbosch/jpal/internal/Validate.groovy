package io.gitlab.arturbosch.jpal.internal;

/**
 * @author artur
 */
public final class Validate {

	private Validate() {
	}

	public static void isTrue(final boolean expression, final String message) {
		if (!expression) {
			throw new IllegalArgumentException(message);
		}
	}

	public static <T> T notNull(T object) {
		if (object == null) {
			throw new IllegalArgumentException("Provided argument is null!");
		}
		return object;
	}

	public static <T> T notNull(T object, String message) {
		if (object == null) {
			throw new IllegalArgumentException(message);
		}
		return object;
	}
}
