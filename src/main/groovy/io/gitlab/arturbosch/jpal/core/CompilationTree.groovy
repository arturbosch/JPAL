package io.gitlab.arturbosch.jpal.core

import com.github.javaparser.ASTHelper
import com.github.javaparser.JavaParser
import com.github.javaparser.ParseException
import com.github.javaparser.TokenMgrException
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.ImportDeclaration
import com.github.javaparser.ast.expr.MethodCallExpr
import com.github.javaparser.ast.type.ClassOrInterfaceType
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
class CompilationTree {

	private static SmartCache<String, Path> qualifiedNameToPathCache =
			new SmartCache<String, Path>()
	private static SmartCache<Path, CompilationUnit> pathToCompilationUnitCache =
			new SmartCache<Path, CompilationUnit>()

	private static Path root

	private static Optional<CompilationUnit> getUnit(Path path) {
		return Optional.ofNullable(pathToCompilationUnitCache.getOrDefault(path))
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

	static Optional<Path> findReferencedType(QualifiedType qualifiedType) {

		def maybePath = Optional.ofNullable(qualifiedNameToPathCache.getOrDefault(qualifiedType.name))

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

	static int findReferencesFor(QualifiedType qualifiedType) {
		int references = 0
		findReferencesFor(qualifiedType, { references++ })
		return references
	}

	static int countMethodInvocations(QualifiedType qualifiedType, Collection<String> methods) {
		int calls = 0
		findReferencesFor(qualifiedType, {
			calls = ASTHelper.getNodesByType(it, MethodCallExpr.class)
					.stream()
					.filter { methods.contains(it.name) }
					.mapToInt { 1 }
					.sum()
		})
		return calls
	}

	static void findReferencesFor(QualifiedType qualifiedType, Consumer<CompilationUnit> code) {
		def walker = getJavaFilteredFileStream()
		walker.forEach {
			getCompilationUnit(it)
					.ifPresent {

				List<ImportDeclaration> imports = it.imports

				Optional<ImportDeclaration> maybeImport = imports.stream()
						.filter { it.name.toStringWithoutComments() == qualifiedType.name }
						.findFirst()

				if (maybeImport.isPresent()) {
					code.accept(it)
				} else if (searchForTypeWithinUnit(it, qualifiedType)) {
					code.accept(it)
				}

			}

		}
		StreamCloser.quietly(walker)
	}

	private static Stream<Path> getJavaFilteredFileStream() {
		Validate.notNull(root, "Compilation tree must be initialized first!")
		Files.walk(root).filter { it.toString().endsWith(".java") }
	}

	static boolean searchForTypeWithinUnit(CompilationUnit unit, QualifiedType qualifiedType) {
		def shortName = qualifiedType.shortName()
		def types = ASTHelper.getNodesByType(unit, ClassOrInterfaceType.class)
		return types.any { it.name == shortName }
	}

	static Optional<CompilationUnit> getCompilationUnit(Path path) {
		def maybeUnit = getUnit(path)
		def unit
		if (maybeUnit.isPresent()) {
			unit = maybeUnit
		} else {
			unit = compileFor(path)
		}
		unit
	}

	static def registerRoot(Path path) {
		root = path
	}

	static def reset() {
		qualifiedNameToPathCache.reset()
		pathToCompilationUnitCache.reset()
	}
}
