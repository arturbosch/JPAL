package io.gitlab.arturbosch.jpal.internal

import com.github.javaparser.printer.PrettyPrinterConfiguration

import java.util.function.Function

/**
 * @author Artur Bosch
 */
final class Printer {

	static final PrettyPrinterConfiguration NO_COMMENTS =
			new PrettyPrinterConfiguration()
					.setPrintComments(false)
					.setPrintJavaDoc(false)

	static final Function<PrettyPrinterConfiguration, PrettyPrintVisitor> FACTORY = { new PrettyPrintVisitor(it) }

	private Printer() {}

	static String toString(com.github.javaparser.ast.Node node) {
		PrettyPrintVisitor visitor = FACTORY.apply(NO_COMMENTS)
		node.accept(visitor, null)
		return visitor.source
	}
}
