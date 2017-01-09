package io.gitlab.arturbosch.jpal.resolve.symbols

import com.github.javaparser.ast.expr.ObjectCreationExpr
import com.github.javaparser.ast.expr.SimpleName
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import io.gitlab.arturbosch.jpal.resolve.QualifiedType

/**
 * @author Artur Bosch
 */
@EqualsAndHashCode(callSuper = true)
@ToString(includePackage = false, includeSuper = true, includeNames = false)
class ObjectCreationSymbolReference extends SymbolReference {
	ObjectCreationExpr expression

	ObjectCreationSymbolReference(SimpleName symbol, QualifiedType qualifiedType, ObjectCreationExpr expression) {
		super(symbol, qualifiedType)
		this.expression = expression
	}
}
