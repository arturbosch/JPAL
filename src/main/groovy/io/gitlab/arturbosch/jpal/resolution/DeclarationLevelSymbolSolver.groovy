package io.gitlab.arturbosch.jpal.resolution

import com.github.javaparser.ast.Node
import com.github.javaparser.ast.body.VariableDeclarator
import com.github.javaparser.ast.expr.SimpleName
import com.github.javaparser.ast.expr.VariableDeclarationExpr
import groovy.transform.CompileStatic
import io.gitlab.arturbosch.jpal.core.CompilationInfo
import io.gitlab.arturbosch.jpal.core.CompilationStorage
import io.gitlab.arturbosch.jpal.resolve.Resolver
import io.gitlab.arturbosch.jpal.resolve.symbols.LocaleVariableSymbolReference
import io.gitlab.arturbosch.jpal.resolve.symbols.SymbolReference

/**
 * @author Artur Bosch
 */
@CompileStatic
final class DeclarationLevelSymbolSolver implements Solver {

	private Resolver resolver
	private CompilationStorage storage

	DeclarationLevelSymbolSolver(Resolver resolver) {
		this.storage = storage
		this.resolver = resolver
	}

	@Override
	Optional<? extends SymbolReference> resolve(SimpleName symbol, CompilationInfo info) {
		def parent = symbol.parentNode
		def symbolReference = resolveVariableDeclaration(parent, symbol, info)
		return symbolReference
	}

	private Optional<LocaleVariableSymbolReference> resolveVariableDeclaration(Optional<Node> parent,
																			   SimpleName symbol, CompilationInfo info) {
		return parent.filter { it instanceof VariableDeclarator }
				.map { (it as VariableDeclarator).parentNode }
				.filter { it.isPresent() && it.get() instanceof VariableDeclarationExpr }
				.map { (it.get() as VariableDeclarationExpr) }
				.map {
			def qualifiedType = resolver.getQualifiedType(info.data, it.commonType)
			new LocaleVariableSymbolReference(symbol, qualifiedType, it)
		}
	}

}
