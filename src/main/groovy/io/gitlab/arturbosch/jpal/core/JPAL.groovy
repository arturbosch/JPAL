package io.gitlab.arturbosch.jpal.core

import io.gitlab.arturbosch.jpal.internal.Validate

import java.nio.file.Path

/**
 * Key class to create a compilation storage.
 *
 * @author Artur Bosch
 */
final class JPAL {

	private JPAL() {}

	/**
	 * Initialize the compilation storage and compiles all sub paths of
	 * given root path. All created compilation info instances will
	 * execute the given processor if processor is provided.
	 *
	 * @param root project path
	 * @param processor compilation info processor, can be null
	 * @return the storage
	 */
	static <T> CompilationStorage 'new'(Path root, CompilationInfoProcessor<T> processor = null) {
		Validate.isTrue(root != null, "Project path must not be null!")
		return new DefaultCompilationStorage(processor).initialize(root)
	}

	/**
	 * Creates a compilation storage without initializing or compiling any sources.
	 * An updatable storage can compile additional or replace existing classes whenever needed.
	 * Specified processor will also run on each additional compilation info.
	 *
	 * @param processor compilation info processor, can be null
	 * @return the only reference to this compilation unit
	 */
	static <T> UpdatableCompilationStorage updatable(CompilationInfoProcessor<T> processor = null) {
		return new UpdatableDefaultCompilationStorage(processor)
	}

	/**
	 * Creates a compilation storage and compiles any sources found down the root path.
	 * An updatable storage can compile additional or replace existing classes whenever needed.
	 * Specified processor will also run on each additional compilation info.
	 *
	 * @param root project root path
	 * @param processor compilation info processor, can be null
	 * @return the only reference to this compilation unit
	 */
	static <T> UpdatableCompilationStorage initializedUpdatable(Path root, CompilationInfoProcessor<T> processor = null) {
		Validate.isTrue(root != null, "Project path must not be null!")
		return new UpdatableDefaultCompilationStorage(processor).initialize(root) as UpdatableCompilationStorage
	}

	/**
	 * Creates a configuration builder for compilation storage's.
	 *
	 * @return builder for compilation storage's
	 */
	static CompilationStorageBuilder builder() {
		return new CompilationStorageBuilder()
	}

}
