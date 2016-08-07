package io.gitlab.arturbosch.jpal.core

import com.github.javaparser.ASTHelper
import com.github.javaparser.JavaParser
import com.github.javaparser.ParseException
import com.github.javaparser.TokenMgrException
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.ImportDeclaration
import com.github.javaparser.ast.expr.MethodCallExpr
import io.gitlab.arturbosch.jpal.ast.TypeHelper
import io.gitlab.arturbosch.jpal.internal.SmartCache
import io.gitlab.arturbosch.jpal.internal.StreamCloser
import io.gitlab.arturbosch.jpal.internal.Validate
import io.gitlab.arturbosch.jpal.resolve.QualifiedType
import org.codehaus.groovy.runtime.IOGroovyMethods

import java.nio.file.Files
import java.nio.file.Path
import java.util.function.Consumer
import java.util.stream.Stream

/**
 * @author artur
 */
final class CompilationTree {

	private static Path root

	private static SmartCache<String, Path> qualifiedNameToPathCache = new SmartCache<String, Path>()
	private static SmartCache<Path, CompilationUnit> pathToCompilationUnitCache =
			new SmartCache<Path, CompilationUnit>()

	/**
	 * Registers the root. Needs to be called before any action on compilation tree should be used.
	 * @param path project root path
	 */
	static void registerRoot(Path path) {
		root = Validate.notNull(path)
	}

	/**
	 * @return tests if the compilation tree was initialized
	 */
	static boolean isInitialized() {
		return root != null
	}

	/**
	 * Resets the caches compilation units. Mainly used for tests.
	 */
	static void reset() {
		qualifiedNameToPathCache.reset()
		pathToCompilationUnitCache.reset()
	}

	/**
	 * Searches for the compilation unit which should be located beneath given path.
	 * If given path is not present within the internal cache, this method tries to
	 * create a new compilation unit for this path.
	 *
	 * @param path path to a class
	 * @return maybe the compilation unit if no compile errors occur
	 */
	static Optional<CompilationUnit> findCompilationUnit(Path path) {
		def maybeUnit = pathToCompilationUnitCache.get(path)
		return maybeUnit.isPresent() ? maybeUnit : compileFor(path)
	}

	/**
	 * Searches for the compilation unit which is described in given qualified type.
	 * This method make calls to the file system if no path for this type was cached before.
	 *
	 * @param qualifiedType given type
	 * @return maybe the compilation unit to this type, can be empty if no matching path is found
	 * or the path is found but the compilation uni cannot be created
	 */
	static Optional<CompilationUnit> findCompilationUnit(QualifiedType qualifiedType) {
		Validate.notNull(qualifiedType)
		return findPathFor(qualifiedType).map { compileFor(it) }
				.map { it.isPresent() ? it.get() : Optional.empty() } as Optional<CompilationUnit>

	}

	/**
	 * Searches for the corresponding path of given ualified type.
	 *
	 * @param qualifiedType given type
	 * @return maybe the path it is reachable from the root
	 */
	static Optional<Path> findPathFor(QualifiedType qualifiedType) {
		def maybePath = qualifiedNameToPathCache.get(qualifiedType.name)

		if (maybePath.isPresent()) {
			return maybePath
		} else {
			def search = qualifiedType.asStringPathToJavaFile()

			def walker = getJavaFilteredFileStream()
			def pathToQualifier = walker
					.filter { it.endsWith(search) }
					.findFirst()
					.map { it.toAbsolutePath().normalize() }
			StreamCloser.quietly(walker)

			pathToQualifier.ifPresent { qualifiedNameToPathCache.put(qualifiedType.name, it) }

			return pathToQualifier
		}
	}

	private static Stream<Path> getJavaFilteredFileStream() {
		Validate.notNull(root, "Compilation tree must be initialized first!")
		return Files.walk(root).filter { it.toString().endsWith(".java") }
				.filter { it.toString() != "package-info.java" } as Stream<Path>
	}

	private static Optional<CompilationUnit> compileFor(Path path) {
		return IOGroovyMethods.withCloseable(Files.newInputStream(path)) {
			try {
				CompilationUnit compilationUnit = JavaParser.parse(it)
				pathToCompilationUnitCache.put(path, compilationUnit)
				Optional.of(compilationUnit)
			} catch (ParseException | TokenMgrException ignored) {
				Optional.empty()
			}
		}
	}

}
