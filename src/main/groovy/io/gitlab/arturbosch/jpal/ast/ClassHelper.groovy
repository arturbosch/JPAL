package io.gitlab.arturbosch.jpal.ast

import com.github.javaparser.ast.Node
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import io.gitlab.arturbosch.jpal.internal.Validate

/**
 * Provides static helper methods to work with {@code ClassOrInterfaceDeclaration}.
 *
 * @author artur
 */
final class ClassHelper {

	private ClassHelper() {}

	/**
	 * Tests if the given node is within a class with given name.
	 * This method is useful if you deal with inner classes and you need to know in which you are.
	 *
	 * @param node given node
	 * @param className class name to test
	 * @return true if node is in a class with given name
	 */
	static boolean inClassScope(Node node, String className) {
		return NodeHelper.findDeclaringClass(node)
				.filter { it.name == className }
				.isPresent()
	}

	/**
	 * @return true if class is empty
	 */
	static boolean isEmptyBody(ClassOrInterfaceDeclaration n) {
		return Validate.notNull(n).members.empty
	}

	/**
	 * @return true if class has no methods
	 */
	static boolean hasNoMethods(ClassOrInterfaceDeclaration n) {
		return Validate.notNull(n).members.grep { it instanceof MethodDeclaration }.isEmpty()
	}

}
