package io.gitlab.arturbosch.jpal.core

import com.github.javaparser.JavaParser
import com.github.javaparser.ParseException
import com.github.javaparser.TokenMgrException
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovy.util.logging.Log
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
@SuppressWarnings("GroovyMissingReturnStatement")
class JavaCompilationParser {

	private CompilationInfoProcessor processor

	@PackageScope
	JavaCompilationParser(CompilationInfoProcessor processor = null) {
		this.processor = processor
	}

	Optional<CompilationInfo> compile(Path path) {
		def result = null
		IOGroovyMethods.withCloseable(Files.newInputStream(path)) {
			try {
				def unit = JavaParser.parse(it)
				if (unit.types.isEmpty()) return Optional.empty()
				result = processor ? CompilationInfo.of(unit, path, processor) :
						CompilationInfo.of(unit, path)
			} catch (ParseException | TokenMgrException error) {
				log.log(Level.SEVERE, "Error while compiling $path occurred", error)
			}
		}
		return Optional.ofNullable(result) as Optional<CompilationInfo>
	}

	Optional<CompilationInfo> compileFromCode(Path path, String code) {
		def result = null
		try {
			def unit = JavaParser.parse(code)
			if (unit.types.isEmpty()) return Optional.empty()
			result = processor ? CompilationInfo.of(unit, path, processor) :
					CompilationInfo.of(unit, path)
		} catch (ParseException | TokenMgrException error) {
			log.log(Level.SEVERE, "Error while compiling $path occurred", error)
		}
		return Optional.ofNullable(result) as Optional<CompilationInfo>
	}

}
