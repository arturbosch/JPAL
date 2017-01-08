package io.gitlab.arturbosch.jpal.core

import com.github.javaparser.JavaParser
import com.github.javaparser.ParseException
import com.github.javaparser.TokenMgrException
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.TypeDeclaration
import groovy.transform.CompileStatic
import io.gitlab.arturbosch.jpal.ast.TypeHelper
import io.gitlab.arturbosch.jpal.internal.SmartCache
import io.gitlab.arturbosch.jpal.internal.StreamCloser
import io.gitlab.arturbosch.jpal.internal.Validate
import io.gitlab.arturbosch.jpal.resolve.QualifiedType
import org.codehaus.groovy.runtime.IOGroovyMethods

import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ForkJoinPool
import java.util.logging.Level
import java.util.logging.Logger
import java.util.stream.Collectors
import java.util.stream.Stream

/**
 * Adds cross referencing ability to javaparser by storing all compilation units
 * within a compilation info, which extends usability by providing the qualified type
 * and path etc to the compilation unit.
 *
 * This class is intended to be initialized before using javaparser. For this, we have
 * to provide a project base path and call {@code CompilationStorage.create ( projectPath )}.
 * The create method returns a reference to the singleton compilation storage.
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
 * def maybeInfo = CompilationStorage.getCompilationInfo(path)
 * def maybeInfo = CompilationStorage.getCompilationInfo(qualifiedType)
 *}
 *
 * Don't use the compilation storage without initializing it.
 * If unsure call {@code CompilationStorage.isInitialized ( )}
 *
 * @author artur
 */
@CompileStatic
final class CompilationStorage {

	private static CompilationStorage storage

	private static CompilationStorage getInstance() {
		Validate.notNull(storage, "Compilation storage not yet initialized!")
		return storage
	}

	private final static Logger LOGGER = Logger.getLogger(CompilationStorage.simpleName)

	private final Path root
	private final CompilationInfoProcessor processor // nullable
	private final SmartCache<QualifiedType, CompilationInfo> typeCache = new SmartCache<>()
	private final SmartCache<Path, CompilationInfo> pathCache = new SmartCache<>()

	private CompilationStorage(Path path, CompilationInfoProcessor processor) {
		this.root = path
		this.processor = processor
	}

	private void createInternal() {

		ForkJoinPool forkJoinPool = new ForkJoinPool(
				Runtime.getRuntime().availableProcessors(),
				ForkJoinPool.defaultForkJoinWorkerThreadFactory, null, true)

		List<CompletableFuture> futures = new ArrayList<>(1000)

		Stream<Path> walker = getJavaFilteredFileStream()
		walker.forEach { path ->
			futures.add(CompletableFuture
					.runAsync({ compileFor(path as Path) }, forkJoinPool)
					.exceptionally { logCompilationFailure(path as Path, it) })
		}

		CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[0])).join()
		forkJoinPool.shutdown()
		StreamCloser.quietly(walker)

	}

	private void compileFor(Path path) {
		IOGroovyMethods.withCloseable(Files.newInputStream(path)) {
			try {
				def unit = JavaParser.parse(it)
				if (unit.types.isEmpty()) return
				def clazz = getFirstDeclaredClass(unit)
				def type = TypeHelper.getQualifiedTypeFromPackage(clazz, unit.packageDeclaration)
				def compilationInfo = processor ?
						CompilationInfo.of(type, unit, path, processor) :
						CompilationInfo.of(type, unit, path)
				typeCache.put(type, compilationInfo)
				pathCache.put(path, compilationInfo)
			} catch (ParseException | TokenMgrException ignored) {
				logCompilationFailure(path, ignored)
			}
		}
	}

	private static TypeDeclaration getFirstDeclaredClass(CompilationUnit compilationUnit) {
		return compilationUnit.getNodesByType(TypeDeclaration.class).first()
	}

	private static void logCompilationFailure(Path path, Throwable error) {
		LOGGER.log(Level.SEVERE, "Error while compiling $path occurred", error)
	}

	private Stream<Path> getJavaFilteredFileStream() {
		return Files.walk(root).parallel().filter { it.toString().endsWith(".java") }
				.filter { !it.toString().endsWith("package-info.java") } as Stream<Path>
	}

	/**
	 * Initialize the compilation unit and compiles all sub paths of
	 * given root path.
	 *
	 * @param root project path
	 * @return the only reference to this compilation unit
	 */
	static CompilationStorage create(Path root) {
		return privateCreate(root, null)
	}

	/**
	 * Initialize the compilation unit and compiles all sub paths of
	 * given root path. All created compilation info instances will
	 * execute the given processor.
	 *
	 * @param root project path
	 * @param processor compilation info processor, can be null
	 * @return the only reference to this compilation unit
	 */
	static <T> CompilationStorage createWithProcessor(Path root, CompilationInfoProcessor<T> processor) {
		return privateCreate(root, processor)
	}

	private static CompilationStorage privateCreate(Path root, CompilationInfoProcessor processor) {
		Validate.isTrue(root != null, "Project path must not be null!")
		storage = new CompilationStorage(root, processor)
		storage.createInternal()
		return storage
	}

	/**
	 * Allows to update an existing compilation info for which the underlying path was relocated.
	 * This method will delete the old compilation info from both caches and compile a new one.
	 *
	 * @param oldPath old file path
	 * @param newPath new file path
	 * @return maybe the new relocated info if no compilation errors occur
	 */
	static Optional<CompilationInfo> updateRelocatedCompilationInfo(Path oldPath, Path newPath) {
		Validate.notNull(oldPath)
		Validate.notNull(newPath)
		Validate.isTrue(Files.exists(newPath), "Relocated path does not exist!")

		getCompilationInfo(oldPath).ifPresent {
			instance.pathCache.remove(oldPath)
			instance.typeCache.remove(it.qualifiedType)
		}
		instance.compileFor(newPath)
		return getCompilationInfo(newPath)
	}

	/**
	 * Updates all given paths by recompiling the underlying compilation units.
	 *
	 * @param paths list of paths to update
	 * @return a list with all updated info, may be empty if all compilations fail
	 */
	static List<CompilationInfo> updateCompilationInfoWithSamePaths(List<Path> paths) {
		List<CompilationInfo> cus = paths.stream().map {
			Validate.notNull(it)
			instance.compileFor(it)
			getCompilationInfo(it)
		}.filter { it.isPresent() }
				.map { it.get() }
				.collect(Collectors.toList())
		return Collections.unmodifiableList(cus)
	}

	/**
	 * @return retrieves all stored qualified type keys
	 */
	static Set<QualifiedType> getAllQualifiedTypes() {
		return Collections.unmodifiableSet(instance.typeCache.internalCache.keySet())
	}

	/**
	 * @return retrieves all stores compilation info's
	 */
	static List<CompilationInfo> getAllCompilationInfo() {
		return Collections.unmodifiableList(instance.typeCache.internalCache.values().toList())
	}

	/**
	 * Maybe a compilation unit for given path is found.
	 *
	 * @param path path for which the info is asked
	 * @return optional of compilation unit
	 */
	static Optional<CompilationInfo> getCompilationInfo(Path path) {
		Validate.notNull(path)
		return instance.pathCache.get(path)
	}

	/**
	 * Maybe a compilation unit for given qualified type is found.
	 *
	 * @param qualifiedType type for which the info is asked
	 * @return optional of compilation unit
	 */
	static Optional<CompilationInfo> getCompilationInfo(QualifiedType qualifiedType) {
		Validate.notNull(qualifiedType)
		def qualifiedOuterType = qualifiedType.asOuterClass()
		return instance.typeCache.get(qualifiedOuterType)
	}

	/**
	 * @return tests if the compilation storage was initialized
	 */
	static boolean isInitialized() {
		return storage != null
	}
}
