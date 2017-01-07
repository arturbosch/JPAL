package io.gitlab.arturbosch.jpal.resolve.symbols

import com.github.javaparser.ast.body.Parameter
import com.github.javaparser.ast.expr.SimpleName
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import io.gitlab.arturbosch.jpal.resolve.QualifiedType

/**
 * @author Artur Bosch
 */
@EqualsAndHashCode
@ToString(includePackage = false)
class ParameterSymbolReference extends VariableSymbolReference {
	Parameter parameter

	ParameterSymbolReference(SimpleName symbol, QualifiedType qualifiedType, Parameter declaration) {
		super(symbol, qualifiedType)
		parameter = declaration
	}
}
