package io.gitlab.arturbosch.jpal.core

import groovy.transform.CompileStatic

import java.nio.file.Path
import java.util.regex.Pattern

/**
 * Lets you configure a compilation storage.
 * Use this builder if you want to specify path filters. Java files which get matched through
 * this filters won't get compiled in the compilation storage.
 *
 * @author Artur Bosch
 */
@CompileStatic
final class CompilationStorageBuilder {

	private Path root = null
	private List<Pattern> filters = new ArrayList<>()
	private boolean updatable = false
	private CompilationInfoProcessor processor = null

	/**
	 * Specifies the root path from which all sub paths get pre compiled
	 * and stored in the compilation storage.
	 * @param root the project root
	 * @return this builder
	 */
	CompilationStorageBuilder withRoot(Path root) {
		this.root = root
		return this
	}

	/**
	 * Specifies the root path from which all sub paths get pre compiled
	 * and stored in the compilation storage.
	 *
	 * @param root the project root
	 * @return this builder
	 */
	CompilationStorageBuilder withFilters(List<Pattern> filters) {
		if (filters != null) this.filters = filters
		return this
	}

	/**
	 * The compilation storage should be updatable by additional added paths or java code as strings.
	 *
	 * @return this builder
	 */
	CompilationStorageBuilder updatable() {
		this.updatable = true
		return this
	}

	/**
	 * Specifies a processor which should be run after a compilation info is created.
	 *
	 * @param processor the processor to run on compilation info's
	 * @return this builder
	 */
	CompilationStorageBuilder withProcessor(CompilationInfoProcessor processor) {
		this.processor = processor
		return this
	}

	/**
	 * Builds the compilation storage with configured or default values.
	 * @return the compilation storage
	 */
	CompilationStorage build() {
		def storage = updatable ?
				root != null ?
						JPAL.initializedUpdatable(root, processor) :
						JPAL.updatable(processor) :
				root != null ?
						JPAL.newInstance(root, processor) : null
		if (storage == null) throw new IllegalStateException("Provided configuration was invalid!")
		return storage
	}
}
