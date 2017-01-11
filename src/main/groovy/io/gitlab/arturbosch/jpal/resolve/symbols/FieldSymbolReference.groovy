package io.gitlab.arturbosch.jpal.resolve.symbols

import com.github.javaparser.ast.body.FieldDeclaration
import com.github.javaparser.ast.expr.SimpleName
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import io.gitlab.arturbosch.jpal.resolve.QualifiedType

/**
 * @author Artur Bosch
 */
@EqualsAndHashCode(callSuper = true)
@ToString(includePackage = false, includeSuper = true, includeNames = false)
class FieldSymbolReference extends VariableSymbolReference {
	FieldDeclaration field

	FieldSymbolReference(SimpleName symbol, QualifiedType qualifiedType, FieldDeclaration declaration) {
		super(symbol, qualifiedType)
		field = declaration
	}
}
