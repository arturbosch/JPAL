package io.gitlab.arturbosch.jpal.resolve.symbols

import com.github.javaparser.ast.expr.SimpleName
import com.github.javaparser.ast.expr.VariableDeclarationExpr
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import io.gitlab.arturbosch.jpal.resolve.QualifiedType

/**
 * @author Artur Bosch
 */
@EqualsAndHashCode
@ToString(includePackage = false)
class LocaleVariableSymbolReference extends VariableSymbolReference {
	VariableDeclarationExpr variable

	LocaleVariableSymbolReference(SimpleName symbol, QualifiedType qualifiedType, VariableDeclarationExpr declaration) {
		super(symbol, qualifiedType)
		variable = declaration
	}
}
