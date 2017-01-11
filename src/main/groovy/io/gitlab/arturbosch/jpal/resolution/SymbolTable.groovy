package io.gitlab.arturbosch.jpal.resolution

import com.github.javaparser.ast.expr.SimpleName
import groovy.transform.CompileStatic
import io.gitlab.arturbosch.jpal.internal.Validate
import io.gitlab.arturbosch.jpal.resolution.symbols.SymbolReference
import io.gitlab.arturbosch.jpal.resolution.util.ConcurrentWeakIdentityHashMap

import java.util.concurrent.ConcurrentMap

/**
 * @author Artur Bosch
 */
@CompileStatic
class SymbolTable {

	private final ConcurrentMap<SimpleName, SymbolReference> cache = new ConcurrentWeakIdentityHashMap<>()

	Optional<SymbolReference> get(SimpleName key) {
		return Optional.ofNullable(cache.get(key))
	}

	void put(SimpleName key, SymbolReference value) {
		Validate.notNull(key, "Key must not be null!")
		Validate.notNull(value, "Value must not be null!")
		cache.put(key, value)
	}

}
