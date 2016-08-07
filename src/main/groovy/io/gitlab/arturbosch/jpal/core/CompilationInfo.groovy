package io.gitlab.arturbosch.jpal.core

import com.github.javaparser.ASTHelper
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.type.ClassOrInterfaceType
import groovy.transform.CompileStatic
import io.gitlab.arturbosch.jpal.ast.TypeHelper
import io.gitlab.arturbosch.jpal.internal.Validate
import io.gitlab.arturbosch.jpal.resolve.QualifiedType

import java.nio.file.Path
import java.util.stream.Collectors

/**
 * Compact information about a compilation unit. Storing the qualified type of
 * root class, a path to the matching file this compilation unit belongs and
 * additional qualified types of it's inner and used classes.
 *
 * @author artur
 */
@CompileStatic
class CompilationInfo {

	final QualifiedType qualifiedType
	final CompilationUnit unit
	final Path path
	final List<QualifiedType> usedTypes
	final Set<QualifiedType> innerClasses

	private CompilationInfo(QualifiedType qualifiedType, CompilationUnit unit, Path path,
							List<QualifiedType> usedTypes, Set<QualifiedType> innerClasses) {

		this.qualifiedType = qualifiedType
		this.unit = unit
		this.path = path
		this.usedTypes = usedTypes
		this.innerClasses = innerClasses
	}

	/**
	 * Factory method to build compilation info's. In most cases you don't need
	 * to build them by yourself, just use CompilationStorage or -Tree.
	 *
	 * @param qualifiedType qualified type of the root class
	 * @param unit corresponding compilation unit
	 * @param path path to the root class file
	 * @return a compilation info
	 */
	static CompilationInfo of(QualifiedType qualifiedType, CompilationUnit unit, Path path) {
		Validate.notNull(qualifiedType)
		Validate.notNull(unit)
		Validate.notNull(path)
		List<QualifiedType> types = extractUsedTypesFromImports(unit)
		def innerClasses = TypeHelper.getQualifiedTypesOfInnerClasses(unit)
		return new CompilationInfo(qualifiedType, unit, path, types, innerClasses)
	}

	private static List extractUsedTypesFromImports(CompilationUnit unit) {
		return unit.imports.stream()
				.filter { !it.isEmptyImportDeclaration() }
				.map { it.name.toStringWithoutComments() }
				.filter { !it.startsWith("java") }
				.map { new QualifiedType(it, QualifiedType.TypeToken.REFERENCE) }
				.collect(Collectors.toList())
	}

	/**
	 * Tests if the given qualified type is referenced by this compilation unit.
	 *
	 * @param type given qualified type
	 * @return true if given type is used within this instance
	 */
	boolean isWithinScope(QualifiedType type) {
		return usedTypes.contains(type) || searchForTypeWithinUnit(type)
	}

	private boolean searchForTypeWithinUnit(QualifiedType qualifiedType) {
		def name = qualifiedType.asOuterClass().name
		def packageName = name.substring(0, name.lastIndexOf("."))

		def samePackage = Optional.ofNullable(unit.package).map { it.packageName == packageName }

		if (samePackage.isPresent()) {
			return searchInternal(qualifiedType, unit)
		} else {
			def sameImport = usedTypes.stream()
					.filter { it.name.contains('*') }
					.map { it.name.substring(0, it.name.lastIndexOf('.')) }
					.filter { it == packageName }
					.findFirst()
			if (sameImport.isPresent()) {
				return searchInternal(qualifiedType, unit)
			}
		}
		return false
	}

	private static boolean searchInternal(QualifiedType qualifiedType, CompilationUnit unit) {
		def shortName = qualifiedType.shortName()
		def types = ASTHelper.getNodesByType(unit, ClassOrInterfaceType.class)
		return types.any { it.name == shortName }
	}

	@Override
	public String toString() {
		return "CompilationInfo{" +
				"qualifiedType=" + qualifiedType +
				'}';
	}
}
