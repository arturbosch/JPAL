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
		Validate.notNull(node)
		Validate.notEmpty(className)
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

	/**
	 * Creates a full signature of given class with respect to anonymous or inner class checks.
	 *
	 * @param n given class type
	 * @return the full unique signature as a string
	 */
	static String createFullSignature(ClassOrInterfaceDeclaration n) {
		Validate.notNull(n)
		String signature = ""
		NodeHelper.findDeclaringClass(n)
				.ifPresent { signature = createSignature(it) + "\$$signature" }
		NodeHelper.findDeclaringMethod(n)
				.ifPresent { signature = signature + "${it.name}\$" }
		return signature + createSignature(n)
	}

	/**
	 * Creates a signature of given class inclusive extends and implement types.
	 * This is the standard case. If you need a precise signature and given class
	 * is a inner or anonymous class, please use {@code createFullSignature}.
	 *
	 * @param n given class type
	 * @return the signature as a string
	 */
	static String createSignature(ClassOrInterfaceDeclaration n) {
		Validate.notNull(n)
		def types = n.typeParameters.join(",")
		def extend = n.extends.join(",")
		def implement = n.implements.join(",")
		return "$n.name${if (types) "<$types>" else ""}${if (extend) " extends $extend" else ""}${if (implement) " implements $implement" else ""}"
	}

}
