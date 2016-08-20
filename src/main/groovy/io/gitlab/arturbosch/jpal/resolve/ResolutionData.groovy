package io.gitlab.arturbosch.jpal.resolve

import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.ImportDeclaration
import com.github.javaparser.ast.PackageDeclaration
import groovy.transform.CompileStatic
import io.gitlab.arturbosch.jpal.ast.TypeHelper
import io.gitlab.arturbosch.jpal.internal.Validate

import java.util.stream.Collectors

/**
 * Holds information of a compilation unit. Used by a resolver to access qualified types.
 *
 * @author artur
 */
@CompileStatic
class ResolutionData {

	final String packageName
	final Map<String, String> imports
	final List<String> importsWithAsterisk;

	/**
	 * From a not null compilation unit a resolution data is constructed containing the
	 * package name and a map of type name to qualified package structure of the imports.
	 */
	static ResolutionData of(CompilationUnit unit) {
		Validate.notNull(unit)
		return new ResolutionData(unit.package, unit.imports)
	}

	/**
	 * Holds the information about a compilation unit. Used by {@code Resolver} to predict
	 * qualified types.
	 *
	 * @param packageDeclaration package to extract name from
	 * @param imports imports to generate convenient type name to package structure map
	 */
	ResolutionData(PackageDeclaration packageDeclaration, List<ImportDeclaration> imports) {
		Validate.notNull(imports)

		this.packageName = Optional.ofNullable(packageDeclaration)
				.map { it.packageName }.orElse(TypeHelper.DEFAULT_PACKAGE)

		this.imports = imports
				.grep { !(it as ImportDeclaration).emptyImportDeclaration }
				.grep { !(it as ImportDeclaration).asterisk }
				.collectEntries { [it.name.name, it.name.toStringWithoutComments()] }

		this.importsWithAsterisk = imports.stream()
				.filter { it.asterisk }
				.map { it.name.toStringWithoutComments() }.collect(Collectors.toList())
	}

}
