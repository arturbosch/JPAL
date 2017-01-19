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
	Optional<CompilationInfo> relocateCompilationInfo(Path oldPath, Path newPath) {
		Validate.notNull(oldPath)
		Validate.notNull(newPath)

		getCompilationInfo(oldPath).ifPresent {
			pathCache.remove(oldPath)
			typeCache.remove(it.qualifiedType)
		}
		createCompilationInfo(newPath)
		return getCompilationInfo(newPath)
	}

	@Override
	List<CompilationInfo> updateCompilationInfo(List<Path> paths) {
		List<CompilationInfo> cus = paths.stream().map {
			Validate.notNull(it)
			createCompilationInfo(it)
			getCompilationInfo(it)
		}.filter { it.isPresent() }
				.map { it.get() }
				.collect(Collectors.toList())
		return Collections.unmodifiableList(cus)
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
	Optional<CompilationInfo> relocateCompilationInfo(Path oldPath, Pair<Path, String> newContent) {
		Validate.notNull(oldPath)
		Validate.notNull(newContent)

		getCompilationInfo(oldPath).ifPresent {
			pathCache.remove(oldPath)
			typeCache.remove(it.qualifiedType)
		}
		createCompilationInfo(newContent.a, newContent.b)
		return getCompilationInfo(newContent.a)
	}

	@Override
	List<CompilationInfo> updateCompilationInfo(Map<Path, String> pathWithContent) {
		Validate.notNull(pathWithContent)
		List<CompilationInfo> cus = pathWithContent.entrySet().stream().map {
			createCompilationInfo(it.key, it.value)
			getCompilationInfo(it.key)
		}.filter { it.isPresent() }
				.map { it.get() }
				.collect(Collectors.toList())
		return Collections.unmodifiableList(cus)
	}

}
