package io.gitlab.arturbosch.jpal.core

import com.github.javaparser.ast.CompilationUnit
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import io.gitlab.arturbosch.jpal.ast.TypeHelper
import io.gitlab.arturbosch.jpal.internal.Validate
import io.gitlab.arturbosch.jpal.resolve.QualifiedType

import java.nio.file.Path

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

	private Object processedObject

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
	 * to build them by yourself, just use DefaultCompilationStorage or -Tree.
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
		def types = TypeHelper.findAllUsedTypes(unit)
		def innerClasses = TypeHelper.getQualifiedTypesOfInnerClasses(unit)
		types = replaceQualifiedTypesOfInnerClasses(types, innerClasses)
		return new CompilationInfo(qualifiedType, unit, path, types, innerClasses)
	}

	/**
	 * Same as above with the difference that CompilationInfoProcessor's can be invoked
	 * on the created CompilationInfo.
	 */
	static CompilationInfo of(QualifiedType qualifiedType, CompilationUnit unit, Path path,
							  CompilationInfoProcessor processor) {
		def info = of(qualifiedType, unit, path)
		info.runProcessor(processor)
		return info
	}

	/**
	 * Tests if the given qualified type is referenced by this compilation unit.
	 *
	 * @param type given qualified type
	 * @return true if given type is used within this instance
	 */
	boolean isWithinScope(QualifiedType type) {
		Validate.notNull(type)
		Validate.isTrue(type.name.contains("."), "Is not a qualified type!")
		return usedTypes.contains(type)
	}

	@PackageScope
	<T> void runProcessor(CompilationInfoProcessor<T> processor) {
		this.processedObject = processor.process(this)
	}

	def <T> T getProcessedObject(Class<T> clazz) {
		if (processedObject.getClass() == clazz) {
			return processedObject as T
		}
		throw new IllegalStateException("Processor is either not set or not the provided type!")
	}

	private static List<QualifiedType> replaceQualifiedTypesOfInnerClasses(List<QualifiedType> types,
																		   Set<QualifiedType> innerClasses) {
		types.collect { QualifiedType type ->
			def find = innerClasses.find { sameNameAndPackage(it, type) }
			if (find) find else type
		}
	}

	private static boolean sameNameAndPackage(QualifiedType first, QualifiedType second) {
		first.shortName == second.shortName && first.onlyPackageName == second.onlyPackageName
	}

	@Override
	String toString() {
		return "CompilationInfo{" +
				"qualifiedType=" + qualifiedType +
				'}'
	}
}
