package io.gitlab.arturbosch.jpal.core

import java.nio.file.Path

/**
 * @author Artur Bosch
 */
interface UpdatableCompilationStorage extends CompilationStorage {

	/**
	 * Allows to update an existing compilation info for which the underlying path was relocated.
	 * This method will delete the old compilation info from both caches and compile a new one.
	 *
	 * @param oldPath old file path
	 * @param newPath new file path
	 * @return maybe the new relocated info if no compilation errors occur
	 */
	Optional<CompilationInfo> updateRelocatedCompilationInfo(Path oldPath, Path newPath)

	/**
	 * Updates all given paths by recompiling the underlying compilation units.
	 *
	 * @param paths list of paths to update
	 * @return a list with all updated info, may be empty if all compilations fail
	 */
	List<CompilationInfo> updateCompilationInfoWithSamePaths(List<Path> paths)
}