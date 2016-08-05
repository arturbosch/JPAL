package io.gitlab.arturbosch.jpal.internal;

/**
 * @author artur
 */
public final class Numbers {

	private Numbers() {
		throw new InstantiationError();
	}

	public static int toInt(final String str, final int defaultValue) {
		if (str == null) {
			return defaultValue;
		}
		try {
			return Integer.parseInt(str);
		} catch (final NumberFormatException ignored) {
			return defaultValue;
		}
	}

	public static double toDouble(final String str, final double defaultValue) {
		if (str == null) {
			return defaultValue;
		}
		try {
			return Double.parseDouble(str);
		} catch (final NumberFormatException ignored) {
			return defaultValue;
		}
	}

}
