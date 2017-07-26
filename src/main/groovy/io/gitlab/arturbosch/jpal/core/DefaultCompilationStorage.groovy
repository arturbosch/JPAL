package io.gitlab.arturbosch.jpal.core

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovy.util.logging.Log
import io.gitlab.arturbosch.jpal.ast.TypeHelper
import io.gitlab.arturbosch.jpal.internal.PrefixedThreadFactory
import io.gitlab.arturbosch.jpal.internal.SmartCache
import io.gitlab.arturbosch.jpal.internal.StreamCloser
import io.gitlab.arturbosch.jpal.internal.Validate
import io.gitlab.arturbosch.jpal.resolution.QualifiedType
import io.gitlab.arturbosch.jpal.resolution.solvers.TypeSolver

import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.logging.Level
import java.util.regex.Pattern
import java.util.stream.Collectors
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

	private static final DEFAULT_PACKAGE = "<default>"

	protected String rootPackageName = DEFAULT_PACKAGE

	protected final SmartCache<QualifiedType, CompilationInfo> typeCache = new SmartCache<>()
	protected final SmartCache<Path, CompilationInfo> pathCache = new SmartCache<>()

	protected final JavaCompilationParser parser
	protected final CompilationInfoProcessor processor
	protected final TypeSolver typeSolver = new TypeSolver(this)
	protected final List<Pattern> pathFilters

	@PackageScope
	DefaultCompilationStorage(CompilationInfoProcessor processor = null, List<Pattern> pathFilters = new ArrayList<>()) {
		this.processor = processor
		this.pathFilters = pathFilters
		this.parser = new JavaCompilationParser()
	}

	@PackageScope
	CompilationStorage initialize(Path root) {

		def threadPool = Executors.newFixedThreadPool(Runtime.runtime.availableProcessors(),
				new PrefixedThreadFactory("jpal"))

		// first build compilation info's foundation
		Stream<Path> walker = getJavaFilteredFileStream(root)
		List<CompletableFuture> futures = walker.collect { Path path ->
			CompletableFuture
					.runAsync({ createCompilationInfo(path) }, threadPool)
					.exceptionally { log.log(Level.WARNING, "Error compiling $path:", it) }
		}
		CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[0])).join()

		// search for used types after compilation as star imports are else not resolvable
		futures = allCompilationInfo.collect { info ->
			CompletableFuture
					.runAsync({ info.findUsedTypes(typeSolver) }, threadPool)
					.thenRun { if (processor) info.runProcessor(processor) }
					.exceptionally { log.log(Level.WARNING, "Error finding used types for $info.path:", it) }
		}
		CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[0])).join()

		threadPool.shutdown()
		StreamCloser.quietly(walker)

		determineRootPackageName()
		return this
	}

	@PackageScope
	void setRootPackageName(String name) {
		rootPackageName = name
	}

	protected void determineRootPackageName() {
		List<String> packages = allCompilationInfo.stream()
				.map { it.qualifiedType }
				.map { it.onlyPackageName }
				.filter { it != TypeHelper.DEFAULT_PACKAGE }
				.collect(Collectors.toList())
		rootPackageName = PackageHelper.determineRootPackageName(packages)
	}

	private static Stream<Path> getJavaFilteredFileStream(Path root) {
		return Files.walk(root).parallel()
				.filter { it.toString().endsWith(".java") }
				.filter { !it.toString().endsWith("module-info.java") }
				.filter { !it.toString().endsWith("package-info.java") } as Stream<Path>
	}

	@Override
	boolean isDefaultPackageRootPackage() {
		return rootPackageName == TypeHelper.DEFAULT_PACKAGE
	}

	@Override
	String getRootPackageName() {
		return rootPackageName
	}

	@Override
	Set<QualifiedType> getAllQualifiedTypes() {
		return Collections.unmodifiableSet(typeCache.keys())
	}

	@Override
	Collection<CompilationInfo> getAllCompilationInfo() {
		return Collections.unmodifiableCollection(typeCache.values())
	}

	@Override
	Optional<CompilationInfo> getCompilationInfo(Path path) {
		Validate.notNull(path)
		return pathCache.get(path)
	}

	@Override
	Optional<CompilationInfo> getCompilationInfo(QualifiedType qualifiedType) {
		Validate.notNull(qualifiedType)
		def qualifiedOuterType = qualifiedType.asOuterClass()
		return typeCache.get(qualifiedOuterType)
	}

	/**
	 * Compiles from path or source code and return the compilation info.
	 * May be null!
	 */
	protected CompilationInfo createCompilationInfo(Path path, String code = null) {
		if (isPackageOrModuleInfo(path)) return null
		if (isFiltered(path)) return null
		def compile = code ? parser.compileFromCode(path, code) : parser.compile(path)
		compile.ifPresent {
			typeCache.put(it.qualifiedType, it)
			pathCache.put(path, it)
		}
		return compile.orElse(null)
	}

	private boolean isFiltered(path) {
		!path.toString().endsWith(".java") || pathFilters.any { it.matcher(path.toString()).find() }
	}

	private static boolean isPackageOrModuleInfo(Path path) {
		def filename = path.fileName.toString()
		return filename == "package-info.java" || filename == "module-info.java"
	}

	/**
	 * Postprocessing of compilation info's. Mainly due to the reason that type resolver
	 * needs a full storage for finding used types. Parameter and result may be null!
	 */
	protected CompilationInfo findTypesAndRunProcessor(CompilationInfo info) {
		if (info) {
			info.findUsedTypes(typeSolver)
			if (processor) info.runProcessor(processor)
		}
		return info
	}

}
