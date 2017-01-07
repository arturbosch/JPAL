package io.gitlab.arturbosch.jpal.resolve.symbols

import com.github.javaparser.ast.expr.SimpleName
import groovy.transform.TupleConstructor
import io.gitlab.arturbosch.jpal.resolve.QualifiedType

/**
 * @author Artur Bosch
 */
@TupleConstructor
abstract class SymbolReference {
	SimpleName symbol
	QualifiedType qualifiedType
}