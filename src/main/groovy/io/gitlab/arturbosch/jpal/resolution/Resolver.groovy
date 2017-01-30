package io.gitlab.arturbosch.jpal.resolution

import com.github.javaparser.ast.expr.SimpleName
import com.github.javaparser.ast.type.Type
import io.gitlab.arturbosch.jpal.core.CompilationInfo
import io.gitlab.arturbosch.jpal.core.CompilationStorage
import io.gitlab.arturbosch.jpal.resolution.solvers.Solver
import io.gitlab.arturbosch.jpal.resolution.solvers.SymbolSolver
import io.gitlab.arturbosch.jpal.resolution.solvers.TypeSolver
import io.gitlab.arturbosch.jpal.resolution.symbols.SymbolReference

/**
 * @author Artur Bosch
 */
final class Resolver implements Solver {

	private final CompilationStorage storage
	private final TypeSolver typeSolver
	private final SymbolSolver symbolSolver

	Resolver(CompilationStorage storage) {
		this.storage = storage
		typeSolver = new TypeSolver(storage)
		symbolSolver = new SymbolSolver(storage)
	}

	@Override
	Optional<? extends SymbolReference> resolve(SimpleName symbol, CompilationInfo info) {
		return symbolSolver.resolve(symbol, info)
	}

	QualifiedType resolveType(Type type, CompilationInfo info) {
		return typeSolver.getQualifiedType(info.data, type)
	}

	CompilationStorage getStorage() {
		return storage
	}
}
