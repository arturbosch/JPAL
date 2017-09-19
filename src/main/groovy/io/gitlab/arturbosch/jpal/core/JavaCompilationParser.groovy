package io.gitlab.arturbosch.jpal.core

import com.github.javaparser.JavaParser
import com.github.javaparser.ParseProblemException
import com.github.javaparser.ParseResult
import com.github.javaparser.ParseStart
import com.github.javaparser.ParserConfiguration
import com.github.javaparser.Providers
import com.github.javaparser.ast.CompilationUnit
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovy.util.logging.Log

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

	private final ParserConfiguration configuration

	JavaCompilationParser(ParserConfiguration configuration = JavaParser.staticConfiguration) {
		this.configuration = configuration
	}

	Optional<CompilationInfo> compile(Path path) {
		def parseResult = new JavaParser(configuration).parse(ParseStart.COMPILATION_UNIT, Providers.provider(path))
		return internalCompile(parseResult, path)
	}

	private static Optional<CompilationInfo> internalCompile(ParseResult<CompilationUnit> parseResult, Path path) {
		if (parseResult.isSuccessful()) {
			def unit = parseResult.getResult().get()
			if (unit.types.isEmpty()) return Optional.empty()
			return Optional.of(CompilationInfo.of(unit, path))
		}
		def message = new ParseProblemException(parseResult.getProblems()).message
		log.log(Level.SEVERE, "Error while compiling $path occurred")
		log.log(Level.SEVERE, message)
		return Optional.empty()
	}


	Optional<CompilationInfo> compileFromCode(Path path, String code) {
		def parseResult = new JavaParser(configuration).parse(ParseStart.COMPILATION_UNIT, Providers.provider(code))
		return internalCompile(parseResult, path)
	}

}
