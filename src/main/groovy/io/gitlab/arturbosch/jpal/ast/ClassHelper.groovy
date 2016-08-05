package io.gitlab.arturbosch.jpal.ast

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
