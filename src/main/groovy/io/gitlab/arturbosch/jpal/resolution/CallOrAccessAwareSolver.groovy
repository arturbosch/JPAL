package io.gitlab.arturbosch.jpal.resolution

import com.github.javaparser.ast.expr.FieldAccessExpr
import com.github.javaparser.ast.expr.MethodCallExpr
import com.github.javaparser.ast.expr.SimpleName
import com.github.javaparser.ast.expr.ThisExpr
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.FirstParam

/**
 * @author Artur Bosch
 */
abstract class CallOrAccessAwareSolver {

	protected static <T, U> Optional<U> withNotNull(T object, @ClosureParams(FirstParam.class) Closure<Optional<U>> block) {
		if (object) {
			return block.call(object)
		}
		Optional.empty()
	}

	protected static MethodCallExpr asMethodCall(SimpleName symbol) {
		return symbol.getParentNode()
				.filter { it instanceof MethodCallExpr }
				.map { it as MethodCallExpr }
				.orElse(null)
	}

	protected static FieldAccessExpr asFieldAccess(SimpleName symbol) {
		return symbol.getParentNode()
				.filter { it instanceof FieldAccessExpr }
				.map { it as FieldAccessExpr }
				.orElse(null)
	}

	protected static boolean isThisAccess(FieldAccessExpr fieldAccessExpr) {
		def scope = fieldAccessExpr.scope
		return !scope.isPresent() || scope.filter { it instanceof ThisExpr }.isPresent()
	}

	protected static boolean isThisAccess(MethodCallExpr methodCallExpr) {
		def scope = methodCallExpr.scope
		return !scope.isPresent() || scope.filter { it instanceof ThisExpr }.isPresent()
	}

}
