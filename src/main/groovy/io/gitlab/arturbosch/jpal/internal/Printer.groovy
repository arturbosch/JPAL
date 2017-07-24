package io.gitlab.arturbosch.jpal.internal

import com.github.javaparser.printer.PrettyPrinter
import com.github.javaparser.printer.PrettyPrinterConfiguration

/**
 * @author Artur Bosch
 */
final class Printer {

	static final PrettyPrinterConfiguration NO_COMMENTS =
			new PrettyPrinterConfiguration().setPrintComments(false)

	static final PrettyPrinter PRETTY_PRINTER = new PrettyPrinter(NO_COMMENTS)

	private Printer() {}

	static String toString(com.github.javaparser.ast.Node node) {
		return PRETTY_PRINTER.print(node)
	}
}
