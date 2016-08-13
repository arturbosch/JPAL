package io.gitlab.arturbosch.jpal

import com.github.javaparser.ASTHelper
import com.github.javaparser.JavaParser
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.FieldDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import groovy.transform.CompileStatic
import org.codehaus.groovy.runtime.IOGroovyMethods

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 * @author artur
 */
@CompileStatic
class Helper {

	static Path BASE_PATH = Paths.get("./src/test/groovy/io/gitlab/arturbosch/jpal/dummies")
	static Path DUMMY = BASE_PATH.resolve("Dummy.java")
	static Path EMPTY_DUMMY = BASE_PATH.resolve("EmptyDummy.java")
	static Path CYCLE_DUMMY = BASE_PATH.resolve("CycleDummy.java")
	static Path NO_PACKAGE_DUMMY = Paths.get("./src/test/resources/NoPackage.java")

	static CompilationUnit compile(Path path) {
		return IOGroovyMethods.withCloseable(Files.newInputStream(path)) {
			JavaParser.parse(it)
		}
	}

	static MethodDeclaration nth(CompilationUnit unit, int n) {
		return ASTHelper.getNodesByType(unit, MethodDeclaration.class).get(n)
	}

	static ClassOrInterfaceDeclaration firstClass(CompilationUnit unit) {
		return ASTHelper.getNodesByType(unit, ClassOrInterfaceDeclaration.class).first()
	}

	static FieldDeclaration nth(ClassOrInterfaceDeclaration clazz, int n) {
		return ASTHelper.getNodesByType(clazz, FieldDeclaration.class).get(n)
	}
}
