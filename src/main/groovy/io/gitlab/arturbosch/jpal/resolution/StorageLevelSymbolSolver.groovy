package io.gitlab.arturbosch.jpal.resolution

import com.github.javaparser.ast.expr.SimpleName
import groovy.transform.CompileStatic
import io.gitlab.arturbosch.jpal.core.CompilationInfo
import io.gitlab.arturbosch.jpal.core.CompilationStorage
import io.gitlab.arturbosch.jpal.resolve.Resolver
import io.gitlab.arturbosch.jpal.resolve.symbols.SymbolReference

/**
 * @author Artur Bosch
 */
@CompileStatic
final class StorageLevelSymbolSolver implements Solver {

	private Resolver resolver
	private CompilationStorage storage

	StorageLevelSymbolSolver(Resolver resolver, CompilationStorage storage) {
		this.storage = storage
		this.resolver = resolver
	}

	@Override
	Optional<? extends SymbolReference> resolve(SimpleName symbol, CompilationInfo info) {
		return null
	}
}
