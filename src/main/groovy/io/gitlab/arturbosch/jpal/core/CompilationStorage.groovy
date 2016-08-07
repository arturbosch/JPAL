package io.gitlab.arturbosch.jpal.core

import com.github.javaparser.ASTHelper
import com.github.javaparser.JavaParser
import com.github.javaparser.ParseException
import com.github.javaparser.TokenMgrException
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
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
import java.util.stream.Stream

import static io.gitlab.arturbosch.jpal.core.CompilationStorage.getFirstDeclaredClass
import static io.gitlab.arturbosch.jpal.core.CompilationStorage.logCompilationFailure

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

	private static CompilationStorage storage;

	private static CompilationStorage getInstance() {
		Validate.notNull(storage, "Compilation storage not yet initialized!")
		return storage;
	}

	private final static Logger LOGGER = Logger.getLogger(CompilationStorage.simpleName)

	private final Path root
	private final SmartCache<QualifiedType, CompilationInfo> typeCache = new SmartCache<>()
	private final SmartCache<Path, CompilationInfo> pathCache = new SmartCache<>()

	private CompilationStorage(Path path) { root = path }

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
				def type = TypeHelper.getQualifiedType(getFirstDeclaredClass(unit), unit.package)
				def compilationInfo = CompilationInfo.of(type, unit, path)
				typeCache.put(type, compilationInfo)
				pathCache.put(path, compilationInfo)
			} catch (ParseException | TokenMgrException ignored) {
			}
		}
	}

	private static ClassOrInterfaceDeclaration getFirstDeclaredClass(CompilationUnit compilationUnit) {
		return ASTHelper.getNodesByType(compilationUnit, ClassOrInterfaceDeclaration.class).first()
	}

	private static void logCompilationFailure(Path path, Throwable error) {
		LOGGER.log(Level.SEVERE, "Error while compiling $path occurred", error)
	}

	private Stream<Path> getJavaFilteredFileStream() {
		return Files.walk(root).parallel().filter { it.toString().endsWith(".java") }
				.filter { it.toString() != "package-info.java" } as Stream<Path>
	}

	/**
	 * Initialize the compilation unit and compiles all sub paths of
	 * given root path.
	 *
	 * @param root project path
	 * @return the only reference to this compilation unit
	 */
	static CompilationStorage create(Path root) {
		Validate.isTrue(root != null, "Project path must not be null!")
		storage = new CompilationStorage(root)
		storage.createInternal()
		return storage
	}

	/**
	 * @return retrieves all stored qualified type keys
	 */
	static Set<QualifiedType> getAllQualifiedTypes() {
		return instance.typeCache.internalCache.keySet()
	}

	/**
	 * @return retrieves all stores compilation info's
	 */
	static List<CompilationInfo> getAllCompilationInfo() {
		return instance.typeCache.internalCache.values().toList()
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
		return instance.typeCache.get(qualifiedType.asOuterClass())
	}

	/**
	 * @return tests if the compilation storage was initialized
	 */
	static boolean isInitialized() {
		return storage != null
	}
}
