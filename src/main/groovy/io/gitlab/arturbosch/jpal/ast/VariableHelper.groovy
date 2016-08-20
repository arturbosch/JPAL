package io.gitlab.arturbosch.jpal.ast

import com.github.javaparser.ast.body.FieldDeclaration
import com.github.javaparser.ast.body.Parameter
import com.github.javaparser.ast.expr.NameExpr
import com.github.javaparser.ast.expr.VariableDeclarationExpr
import groovy.transform.CompileStatic
import io.gitlab.arturbosch.jpal.ast.custom.JpalVariable

import java.util.stream.Collectors

/**
 * Provides static methods to convert fields, parameters and locale variables from javaparser
 * to jpal representation of variables.
 *
 * @author artur
 */
@CompileStatic
class VariableHelper {

	/**
	 * From locale variable to jpal variables as given expression can have many variables.
	 */
	static Set<JpalVariable> toJpalFromLocale(VariableDeclarationExpr variables) {
		return variables.vars.stream().map {
			new JpalVariable(
					it.range.begin.line,
					it.range.begin.column,
					it.range.end.line,
					it.range.end.column,
					variables.modifiers,
					variables.annotations,
					variables.type,
					it.id,
					it.init,
					JpalVariable.Nature.Local)
		}.collect(Collectors.toSet())
	}

	/**
	 * From locale variables to jpal variables.
	 */
	static Set<JpalVariable> toJpalFromLocales(List<VariableDeclarationExpr> declarations) {
		return declarations.stream().map { toJpalFromLocale(it) }
				.flatMap { it.stream() }.collect(Collectors.toSet())
	}

	/**
	 * From field declaration to jpal variables as given declaration can have many variables.
	 */
	static Set<JpalVariable> toJpalFromField(FieldDeclaration field) {
		return field.variables.stream().map {
			new JpalVariable(
					it.range.begin.line,
					it.range.begin.column,
					it.range.end.line,
					it.range.end.column,
					field.modifiers,
					field.annotations,
					field.type,
					it.id,
					it.init,
					JpalVariable.Nature.Local)
		}.collect(Collectors.toSet())
	}

	/**
	 * From field declarations to jpal variables.s
	 */
	static Set<JpalVariable> toJpalFromFields(List<FieldDeclaration> declarations) {
		return declarations.stream().map { toJpalFromField(it) }
				.flatMap { it.stream() }.collect(Collectors.toSet())
	}

	/**
	 * From parameter to jpal variable.
	 */
	static JpalVariable toJpalFromParameter(Parameter parameter) {
		return new JpalVariable(
				parameter.range.begin.line,
				parameter.range.begin.column,
				parameter.range.end.line,
				parameter.range.end.column,
				parameter.modifiers,
				parameter.annotations,
				parameter.type,
				parameter.id,
				new NameExpr(parameter.id.name),
				JpalVariable.Nature.Parameter)
	}

	/**
	 * From parameters to jpal variables.
	 */
	static Set<JpalVariable> toJpalFromParameters(List<Parameter> parameters) {
		return parameters.stream().map { toJpalFromParameter(it) }.collect(Collectors.toSet())
	}

}
