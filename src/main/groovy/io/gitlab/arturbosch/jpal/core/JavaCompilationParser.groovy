package io.gitlab.arturbosch.jpal.core

import com.github.javaparser.JavaParser
import com.github.javaparser.ParseException
import com.github.javaparser.TokenMgrException
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.TypeDeclaration
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovy.util.logging.Log
import io.gitlab.arturbosch.jpal.ast.TypeHelper
import org.codehaus.groovy.runtime.IOGroovyMethods

import java.nio.file.Files
import java.nio.file.Path
import java.util.logging.Level

/**
 * @author Artur Bosch
 */
@CompileStatic
@Log
@PackageScope
class JavaCompilationParser {

	private CompilationInfoProcessor processor

	@PackageScope
	JavaCompilationParser(CompilationInfoProcessor processor = null) {
		this.processor = processor
	}

	@SuppressWarnings("GroovyMissingReturnStatement")
	Optional<CompilationInfo> compile(Path path) {
		def result = null
		IOGroovyMethods.withCloseable(Files.newInputStream(path)) {
			try {
				def unit = JavaParser.parse(it)
				if (unit.types.isEmpty()) return
				def clazz = getFirstDeclaredClass(unit)
				def type = TypeHelper.getQualifiedTypeFromPackage(clazz, unit.packageDeclaration)
				result = processor ? CompilationInfo.of(type, unit, path, processor) :
						CompilationInfo.of(type, unit, path)
			} catch (ParseException | TokenMgrException error) {
				log.log(Level.SEVERE, "Error while compiling $path occurred", error)
			}
		}
		return Optional.ofNullable(result) as Optional<CompilationInfo>
	}

	private static TypeDeclaration getFirstDeclaredClass(CompilationUnit compilationUnit) {
		return compilationUnit.getNodesByType(TypeDeclaration.class).first()
	}

}
