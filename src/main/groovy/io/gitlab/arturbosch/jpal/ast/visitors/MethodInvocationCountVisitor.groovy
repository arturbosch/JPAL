package io.gitlab.arturbosch.jpal.ast.visitors

import com.github.javaparser.ast.expr.MethodCallExpr
import com.github.javaparser.ast.visitor.VoidVisitorAdapter
import groovy.transform.CompileStatic
import io.gitlab.arturbosch.jpal.internal.Printer

/**
 * Visits method call expressions and count them if they match the searched name or
 * just all invocations.
 *
 * @author artur
 */
@CompileStatic
class MethodInvocationCountVisitor extends VoidVisitorAdapter {

	private int count
	private final String searchedName

	MethodInvocationCountVisitor() {
		this.searchedName = ""
	}

	MethodInvocationCountVisitor(String searchedName) {
		this.searchedName = searchedName
	}

	/**
	 * Allows to count all invocations within the method or only invocations for a
	 * specific variable using its name as the searched name provided within the constructor.
	 *
	 * @param n the method within invocations are count
	 * @param arg store additional data here
	 */
	@Override
	void visit(MethodCallExpr n, Object arg) {
		if (searchedName.isEmpty()) {
			count++
		} else {
			n.scope.map { it.toString(Printer.NO_COMMENTS) }
					.filter { (it == searchedName) }
					.ifPresent { count++ }
		}
		super.visit(n, arg)
	}

	int getCount() {
		return count
	}
}
