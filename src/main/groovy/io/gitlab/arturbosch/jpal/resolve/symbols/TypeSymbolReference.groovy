package io.gitlab.arturbosch.jpal.resolve.symbols

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.expr.SimpleName
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import io.gitlab.arturbosch.jpal.resolve.QualifiedType

/**
 * @author Artur Bosch
 */
@EqualsAndHashCode(callSuper = true)
@ToString(includePackage = false, includeSuper = true, includeNames = false)
class TypeSymbolReference extends SymbolReference {
	ClassOrInterfaceDeclaration declaration

	TypeSymbolReference(SimpleName symbol, QualifiedType qualifiedType, ClassOrInterfaceDeclaration declaration) {
		super(symbol, qualifiedType)
		this.declaration = declaration
	}

}
