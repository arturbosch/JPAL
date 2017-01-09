package io.gitlab.arturbosch.jpal.resolve.symbols

import com.github.javaparser.ast.expr.NameExpr
import com.github.javaparser.ast.expr.SimpleName
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import io.gitlab.arturbosch.jpal.resolve.QualifiedType

/**
 * @author Artur Bosch
 */
@EqualsAndHashCode(callSuper = true)
@ToString(includePackage = false, includeSuper = true, includeNames = false)
class SimpleSymbolReference extends SymbolReference {
	SimpleSymbolReference(SimpleName symbol, QualifiedType qualifiedType) {
		super(symbol, qualifiedType)
	}
}