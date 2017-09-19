package io.gitlab.arturbosch.jpal.core

import com.github.javaparser.utils.Pair
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import io.gitlab.arturbosch.jpal.internal.PrefixedThreadFactory
import io.gitlab.arturbosch.jpal.internal.Validate

import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.regex.Pattern
import java.util.stream.Collectors

/**
 * @author Artur Bosch
 */
@CompileStatic
class UpdatableDefaultCompilationStorage extends DefaultCompilationStorage implements UpdatableCompilationStorage {

	private ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.runtime.availableProcessors(),
			new PrefixedThreadFactory("jpal"))

	@PackageScope
	UpdatableDefaultCompilationStorage(CompilationInfoProcessor processor = null,
									   List<Pattern> pathFilters = new ArrayList<>(),
									   JavaCompilationParser parser = null) {
		super(processor, pathFilters, parser)
		Runtime.runtime.addShutdownHook { threadPool.shutdown() }
	}

	@Override
	List<CompilationInfo> relocateCompilationInfo(Map<Path, Path> relocates) {
		Validate.notNull(relocates)

		def futures = relocates.collect { oldPath, newPath ->
			CompletableFuture.supplyAsync({
				getCompilationInfo(oldPath).ifPresent {
					pathCache.remove(oldPath)
					typeCache.remove(it.qualifiedType)
				}
				createCompilationInfo(newPath)
			}, threadPool)
		}

		return awaitAll(futures)
	}

	@Override
	List<CompilationInfo> updateCompilationInfo(List<Path> paths) {
		def futures = paths.collect { path ->
			CompletableFuture.supplyAsync({
				Validate.notNull(path)
				createCompilationInfo(path)
			}, threadPool)
		}
		return awaitAll(futures)
	}

	@Override
	void removeCompilationInfo(List<Path> paths) {
		paths.each { path ->
			getCompilationInfo(path).ifPresent {
				pathCache.remove(path)
				typeCache.remove(it.qualifiedType)
				removePackageName(it.qualifiedType.onlyPackageName)
			}
		}
	}

	@Override
	List<CompilationInfo> relocateCompilationInfoFromSource(Map<Path, Pair<Path, String>> relocates) {
		Validate.notNull(relocates)

		def futures = relocates.collect { oldPath, newContent ->
			CompletableFuture.supplyAsync({
				getCompilationInfo(oldPath).ifPresent {
					pathCache.remove(oldPath)
					typeCache.remove(it.qualifiedType)
				}
				createCompilationInfo(newContent.a, newContent.b)
			}, threadPool)
		}

		return awaitAll(futures)
	}

	@Override
	List<CompilationInfo> updateCompilationInfo(Map<Path, String> pathWithContent) {
		Validate.notNull(pathWithContent)

		def futures = pathWithContent.collect { path, content ->
			CompletableFuture.supplyAsync({
				createCompilationInfo(path, content)
			}, threadPool)
		}
		return awaitAll(futures)
	}

	private List<CompilationInfo> awaitAll(List<CompletableFuture<CompilationInfo>> futures) {
		CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[0])).join()
		def arrayOfFutures = futures.collect {
			it.thenApplyAsync({ findTypesAndRunProcessor(it) }, threadPool)
		}.toArray(new CompletableFuture<?>[0])
		CompletableFuture.allOf(arrayOfFutures).join()
		List<CompilationInfo> result = futures.stream()
				.map { it.get() }
				.filter { it != null }
				.collect(Collectors.toList())
		return Collections.unmodifiableList(result)
	}

}
