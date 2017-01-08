package io.gitlab.arturbosch.jpal.core

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import io.gitlab.arturbosch.jpal.internal.Validate

import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.Collectors

/**
 * @author Artur Bosch
 */
@CompileStatic
class UpdatableDefaultCompilationStorage extends DefaultCompilationStorage implements UpdatableCompilationStorage {

	@PackageScope
	UpdatableDefaultCompilationStorage(Path path, CompilationInfoProcessor processor) {
		super(path, processor)
	}

	Optional<CompilationInfo> updateRelocatedCompilationInfo(Path oldPath, Path newPath) {
		Validate.notNull(oldPath)
		Validate.notNull(newPath)
		Validate.isTrue(Files.exists(newPath), "Relocated path does not exist!")

		getCompilationInfo(oldPath).ifPresent {
			pathCache.remove(oldPath)
			typeCache.remove(it.qualifiedType)
		}
		createCompilationInfo(newPath)
		return getCompilationInfo(newPath)
	}

	List<CompilationInfo> updateCompilationInfoWithSamePaths(List<Path> paths) {
		List<CompilationInfo> cus = paths.stream().map {
			Validate.notNull(it)
			createCompilationInfo(it)
			getCompilationInfo(it)
		}.filter { it.isPresent() }
				.map { it.get() }
				.collect(Collectors.toList())
		return Collections.unmodifiableList(cus)
	}
}
