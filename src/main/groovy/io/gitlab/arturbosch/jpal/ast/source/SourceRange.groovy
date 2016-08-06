package io.gitlab.arturbosch.jpal.ast.source

import com.github.javaparser.ast.Node
import groovy.transform.CompileStatic
import groovy.transform.Immutable
import groovy.transform.ToString

/**
 * A source range is a tuple of source positions and represent a range inside a file.
 *
 * @author artur
 */
@Immutable
@ToString(includePackage = false, includeNames = false)
@CompileStatic
class SourceRange {

	int startLine
	int endLine
	int startColumn
	int endColumn

	static SourceRange of(int startLine, int endLine, int startColumn, int endColumn) {
		return new SourceRange(startLine, endLine, startColumn, endColumn)
	}

	static SourceRange fromNode(Node node) {
		def range = node.getRange()
		return of(range.begin.line, range.end.line, range.begin.column, range.end.column)
	}
}
