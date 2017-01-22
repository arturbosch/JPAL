package io.gitlab.arturbosch.jpal.core

import com.github.javaparser.utils.Pair
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import io.gitlab.arturbosch.jpal.internal.Validate

import java.nio.file.Path
import java.util.stream.Collectors

/**
 * @author Artur Bosch
 */
@CompileStatic
class UpdatableDefaultCompilationStorage extends DefaultCompilationStorage implements UpdatableCompilationStorage {

	@PackageScope
	UpdatableDefaultCompilationStorage(CompilationInfoProcessor processor) {
		super(processor)
	}

	@Override
	List<CompilationInfo> relocateCompilationInfo(Map<Path, Path> relocates) {
		Validate.notNull(relocates)

		def futures = relocates.collect { oldPath, newPath ->
			getCompilationInfo(oldPath).ifPresent {
				pathCache.remove(oldPath)
				typeCache.remove(it.qualifiedType)
			}
			createCompilationInfo(newPath)
		}

		return awaitAll(futures)
	}

	@Override
	List<CompilationInfo> updateCompilationInfo(List<Path> paths) {
		def futures = paths.collect { path ->
			Validate.notNull(path)
			createCompilationInfo(path)
		}
		return awaitAll(futures)
	}

	@Override
	void removeCompilationInfo(List<Path> paths) {
		paths.each { path ->
			getCompilationInfo(path).ifPresent {
				pathCache.remove(path)
				typeCache.remove(it.qualifiedType)
			}
		}
	}

	@Override
	List<CompilationInfo> relocateCompilationInfoFromSource(Map<Path, Pair<Path, String>> relocates) {
		Validate.notNull(relocates)

		def futures = relocates.collect { oldPath, newContent ->
			getCompilationInfo(oldPath).ifPresent {
				pathCache.remove(oldPath)
				typeCache.remove(it.qualifiedType)
			}
			createCompilationInfo(newContent.a, newContent.b)
		}

		return awaitAll(futures)
	}

	@Override
	List<CompilationInfo> updateCompilationInfo(Map<Path, String> pathWithContent) {
		Validate.notNull(pathWithContent)

		def futures = pathWithContent.collect { path, content ->
			createCompilationInfo(path, content)
		}
		return awaitAll(futures)
	}

	private List<CompilationInfo> awaitAll(List<CompilationInfo> infos) {
		List<CompilationInfo> processed = infos.stream()
				.map { findTypesAndRunProcessor(it) }
				.filter { it != null }
				.collect(Collectors.toList())
		return Collections.unmodifiableList(processed)
	}

}
