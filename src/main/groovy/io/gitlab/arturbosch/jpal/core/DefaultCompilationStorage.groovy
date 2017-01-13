package io.gitlab.arturbosch.jpal.core

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovy.util.logging.Log
import io.gitlab.arturbosch.jpal.internal.SmartCache
import io.gitlab.arturbosch.jpal.internal.StreamCloser
import io.gitlab.arturbosch.jpal.internal.Validate
import io.gitlab.arturbosch.jpal.resolution.QualifiedType

import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ForkJoinPool
import java.util.logging.Level
import java.util.stream.Stream

/**
 * Adds cross referencing ability to javaparser by storing all compilation units
 * within a compilation info, which extends usability by providing the qualified type
 * and path etc to the compilation unit.
 *
 * This class is intended to be initialized before using javaparser. For this, we have
 * to provide a project base path and call {@code DefaultCompilationStorage.new ( projectPath )}.
 * The new method returns a reference to the singleton compilation storage.
 *
 * Internally a {@code ForkJoinPool} is used to compile all child paths and store their
 * compilation units in parallel.
 *
 * This is the only way to obtain a reference to the compilation storage, but mostly not
 * necessary as the compilation storage singleton is stored internal and provide a static only api.
 *
 * To obtain compilation info's, use the convenient methods:
 *
 * {@code
 * def maybeInfo = DefaultCompilationStorage.getCompilationInfo(path)
 * def maybeInfo = DefaultCompilationStorage.getCompilationInfo(qualifiedType)
 *}
 *
 * Don't use the compilation storage without initializing it.
 * If unsure call {@code DefaultCompilationStorage.isInitialized ( )}
 *
 * @author artur
 */
@Log
@CompileStatic
class DefaultCompilationStorage implements CompilationStorage {

	private final Path root
	private final JavaCompilationParser parser
	protected final SmartCache<QualifiedType, CompilationInfo> typeCache = new SmartCache<>()
	protected final SmartCache<Path, CompilationInfo> pathCache = new SmartCache<>()

	@PackageScope
	DefaultCompilationStorage(Path path, CompilationInfoProcessor processor, boolean debug) {
		println "CompilationStorage without createInternal()"
		Validate.notNull(path)
		this.root = path
		this.parser = new JavaCompilationParser(processor)
	}

	@PackageScope
	DefaultCompilationStorage(Path path, CompilationInfoProcessor processor) {
		Validate.notNull(path)
		this.root = path
		this.parser = new JavaCompilationParser(processor)
		createInternal()
	}

	Set<QualifiedType> getAllQualifiedTypes() {
		return Collections.unmodifiableSet(typeCache.keys())
	}

	List<CompilationInfo> getAllCompilationInfo() {
		return Collections.unmodifiableList(typeCache.values())
	}

	Optional<CompilationInfo> getCompilationInfo(Path path) {
		Validate.notNull(path)
		return pathCache.get(path)
	}

	Optional<CompilationInfo> getCompilationInfo(QualifiedType qualifiedType) {
		Validate.notNull(qualifiedType)
		def qualifiedOuterType = qualifiedType.asOuterClass()
		return typeCache.get(qualifiedOuterType)
	}

	private void createInternal() {

		ForkJoinPool forkJoinPool = new ForkJoinPool(
				Runtime.getRuntime().availableProcessors(),
				ForkJoinPool.defaultForkJoinWorkerThreadFactory, null, true)

		List<CompletableFuture> futures = new ArrayList<>(1000)

		Stream<Path> walker = getJavaFilteredFileStream()
		walker.forEach { Path path ->
			futures.add(CompletableFuture
					.runAsync({ createCompilationInfo(path) }, forkJoinPool)
					.exceptionally { log.log(Level.WARNING, "Error compiling $path:", it) })
		}

		CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[0])).join()
		forkJoinPool.shutdown()
		StreamCloser.quietly(walker)

	}

	private Stream<Path> getJavaFilteredFileStream() {
		return Files.walk(root).parallel().filter { it.toString().endsWith(".java") }
				.filter { !it.toString().endsWith("package-info.java") } as Stream<Path>
	}

	protected void createCompilationInfo(Path path, String code = null) {
		def compile = code ? parser.compileFromCode(path, code) : parser.compile(path)
		compile.ifPresent {
			typeCache.put(it.qualifiedType, it)
			pathCache.put(path, it)
		}
	}

}
