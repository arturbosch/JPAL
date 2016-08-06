package io.gitlab.arturbosch.jpal.ast

import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.expr.VariableDeclarationExpr
import groovy.transform.CompileStatic
import io.gitlab.arturbosch.jpal.ast.visitors.LocaleVariableVisitor

import java.util.stream.Collectors

/**
 * @author artur
 */
@CompileStatic
class LocaleVariableHelper {

	static List<VariableDeclarationExpr> find(List<MethodDeclaration> methodDeclarations) {
		return methodDeclarations.stream().map { find(it) }
				.flatMap { it.stream() }
				.collect(Collectors.toList())
	}

	static Set<VariableDeclarationExpr> find(MethodDeclaration methodDeclaration) {
		def visitor = new LocaleVariableVisitor()
		methodDeclaration.accept(visitor, null)
		return visitor.variables
	}
}
