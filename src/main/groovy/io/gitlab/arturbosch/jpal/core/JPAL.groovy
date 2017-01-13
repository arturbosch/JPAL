package io.gitlab.arturbosch.jpal.core

import io.gitlab.arturbosch.jpal.internal.Validate

import java.nio.file.Path

/**
 * @author Artur Bosch
 */
final class JPAL {

	private JPAL() {}

	/**
	 * Initialize the compilation storage and compiles all sub paths of
	 * given root path. All created compilation info instances will
	 * execute the given processor.
	 *
	 * @param root project path
	 * @param processor compilation info processor, can be null
	 * @return the only reference to this compilation unit
	 */
	static <T> CompilationStorage 'new'(Path root, CompilationInfoProcessor<T> processor = null) {
		return createInternal(root) { new DefaultCompilationStorage(root, processor) }
	}

	/**
	 * Initialize the compilation storage and compiles all sub paths of
	 * given root path. All created compilation info instances will
	 * execute the given processor. An updatable storage can compile additional
	 * or replace existing classes.
	 *
	 * @param root project path
	 * @param processor compilation info processor, can be null
	 * @return the only reference to this compilation unit
	 */
	static <T> UpdatableCompilationStorage updatable(Path root, CompilationInfoProcessor<T> processor = null) {
		return createInternal(root) {
			new UpdatableDefaultCompilationStorage(root, processor)
		} as UpdatableCompilationStorage
	}

	static <T> UpdatableCompilationStorage updatableFromSource(Path root, CompilationInfoProcessor<T> processor = null) {
		return createInternal(root) {
			new UpdatableDefaultCompilationStorage(root, processor, true)
		} as UpdatableCompilationStorage
	}

	private static CompilationStorage createInternal(Path root, Closure<CompilationStorage> storageCreation) {
		Validate.isTrue(root != null, "Project path must not be null!")
		return storageCreation()
	}

}
