package io.gitlab.arturbosch.jpal

import com.github.javaparser.ASTHelper
import com.github.javaparser.JavaParser
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import org.codehaus.groovy.runtime.IOGroovyMethods

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 * @author artur
 */
class Helper {

	static Path BASE_PATH = Paths.get("./src/test/groovy/io/gitlab/arturbosch/jpal")
	static Path DUMMY = BASE_PATH.resolve("Dummy.java")

	static CompilationUnit compile(Path path) {
		return IOGroovyMethods.withCloseable(Files.newInputStream(path)) {
			JavaParser.parse(it)
		}
	}

	static MethodDeclaration nth(CompilationUnit unit, int n) {
		ASTHelper.getNodesByType(unit, MethodDeclaration.class).get(n)
	}

	static ClassOrInterfaceDeclaration first(CompilationUnit unit) {
		ASTHelper.getNodesByType(unit, ClassOrInterfaceDeclaration.class).first()
	}
}
