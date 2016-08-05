package io.gitlab.arturbosch.jpal.ast

import com.github.javaparser.ASTHelper
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.Node
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.FieldDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.body.ModifierSet
import io.gitlab.arturbosch.jpal.internal.Looper
import io.gitlab.arturbosch.jpal.internal.Validate

import java.util.function.Predicate
import java.util.stream.Collectors

/**
 * Provides static methods to search for specific nodes.
 *
 * @author artur
 */
final class NodeHelper {

	static Predicate<Node> unitPredicate = { it instanceof CompilationUnit }
	static Predicate<Node> classPredicate = { it instanceof ClassOrInterfaceDeclaration }
	static Predicate<Node> methodPredicate = { it instanceof MethodDeclaration }

	private NodeHelper() {}

	/**
	 * Returns a list of private method declarations found within the given node.
	 * @param n node, most often ClassOrInterfaceDeclaration or CompilationUnit
	 * @return list of private method declarations
	 */
	static List<MethodDeclaration> findPrivateMethods(Node n) {
		return findMethods(Validate.notNull(n)).stream()
				.filter { ModifierSet.isPrivate(it.modifiers) }
				.collect(Collectors.toList())
	}

	/**
	 * Returns a list of method declarations found within the given node.
	 * @param n node, most often ClassOrInterfaceDeclaration or CompilationUnit
	 * @return list of method declarations
	 */
	static List<MethodDeclaration> findMethods(Node n) {
		return ASTHelper.getNodesByType(Validate.notNull(n), MethodDeclaration.class)
	}

	/**
	 * Returns a list of private field declarations found within the given node.
	 * @param n node, most often ClassOrInterfaceDeclaration or CompilationUnit
	 * @return list of private field declarations
	 */
	static List<FieldDeclaration> findPrivateFields(Node n) {
		return findFields(Validate.notNull(n)).stream()
				.filter { ModifierSet.isPrivate(it.modifiers) }
				.collect(Collectors.toList())
	}

	/**
	 * Returns a list of field declarations found within the given node.
	 * @param n node, most often ClassOrInterfaceDeclaration or CompilationUnit
	 * @return list of field declarations
	 */
	static List<FieldDeclaration> findFields(Node n) {
		return ASTHelper.getNodesByType(Validate.notNull(n), FieldDeclaration.class)
	}

	/**
	 * Returns a set of names of inner classes which are found within the given node.
	 * @param n node to search for inner classes
	 * @return set of strings
	 */
	static Set<String> findNamesOfInnerClasses(Node n) {
		return ASTHelper.getNodesByType(Validate.notNull(n), ClassOrInterfaceDeclaration.class).stream()
				.filter { it.parentNode instanceof ClassOrInterfaceDeclaration }
				.map { it.name }
				.collect(Collectors.toSet())
	}

	/**
	 * Searches for the class given node is declared in.
	 * @param node given node
	 * @return maybe a class declaration
	 */
	static Optional<ClassOrInterfaceDeclaration> findDeclaringClass(Node node) {
		return findDeclaring(node, classPredicate)
	}

	/**
	 * Searches for the compilation uni given node is declared in.
	 * @param node given node
	 * @return maybe a compilation unit
	 */
	static Optional<CompilationUnit> findDeclaringCompilationUnit(Node node) {
		return findDeclaring(node, unitPredicate)
	}

	/**
	 * Searches for the method given node is declared in.
	 * @param node given node
	 * @return maybe a method declaration
	 */
	static Optional<MethodDeclaration> findDeclaringMethod(Node node) {
		return findDeclaring(node, methodPredicate)
	}

	private static Optional findDeclaring(Node node, Predicate<Node> predicate) {
		Node parent = Validate.notNull(node)
		Looper.loop {
			parent = parent.getParentNode()
		} until { predicate.test(parent) || parent == null }

		return parent == null ? Optional.empty() : Optional.of(parent)
	}
}
