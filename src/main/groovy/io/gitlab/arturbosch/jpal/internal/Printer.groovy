package io.gitlab.arturbosch.jpal.internal

import com.github.javaparser.printer.PrettyPrinterConfiguration

/**
 * @author Artur Bosch
 */
final class Printer {

	public static final PrettyPrinterConfiguration NO_COMMENTS =
			new PrettyPrinterConfiguration().setPrintComments(false)

	private Printer() {}
}
