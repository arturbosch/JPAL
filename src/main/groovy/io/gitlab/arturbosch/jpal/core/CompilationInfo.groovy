package io.gitlab.arturbosch.jpal.core

import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.TypeDeclaration
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import io.gitlab.arturbosch.jpal.ast.TypeHelper
import io.gitlab.arturbosch.jpal.internal.Validate
import io.gitlab.arturbosch.jpal.resolve.QualifiedType
import io.gitlab.arturbosch.jpal.resolve.ResolutionData

import java.nio.file.Path

/**
 * Compact information about a compilation unit. Storing the qualified type of
 * root class, a path to the matching file this compilation unit belongs and
 * additional qualified types of it's inner and used classes.
 *
 * @author artur
 */
@CompileStatic
class CompilationInfo implements Processable {

	final QualifiedType qualifiedType
	final CompilationUnit unit
	final Path path
	final List<QualifiedType> usedTypes
	final TypeDeclaration mainType
	final ResolutionData data
	final Map<QualifiedType, TypeDeclaration> innerClasses

	private CompilationInfo(QualifiedType qualifiedType,
							TypeDeclaration mainType,
							Map<QualifiedType, TypeDeclaration> innerClasses,
							List<QualifiedType> usedTypes,
							CompilationUnit unit,
							Path path) {

		this.innerClasses = innerClasses
		this.mainType = mainType
		this.qualifiedType = qualifiedType
		this.unit = unit
		this.path = path
		this.usedTypes = usedTypes
		this.data = ResolutionData.of(unit)
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
	static CompilationInfo of(CompilationUnit unit, Path path) {
		Validate.notNull(unit)
		Validate.notNull(path)
		def usedTypes = TypeHelper.findAllUsedTypes(unit)
		def mainClassAndInnerClassesPair = TypeHelper.getQualifiedDeclarationsOfInnerClasses(unit)
		QualifiedType qualifiedType = mainClassAndInnerClassesPair.a.a
		TypeDeclaration mainType = mainClassAndInnerClassesPair.a.b
		Map<QualifiedType, TypeDeclaration> innerTypes = mainClassAndInnerClassesPair.b
		usedTypes = replaceQualifiedTypesOfInnerClasses(usedTypes, innerTypes.keySet())
		return new CompilationInfo(qualifiedType, mainType, innerTypes, usedTypes, unit, path)
	}

	/**
	 * Same as above with the difference that CompilationInfoProcessor's can be invoked
	 * on the created CompilationInfo.
	 */
	static CompilationInfo of(CompilationUnit unit, Path path,
							  CompilationInfoProcessor processor) {
		def info = of(unit, path)
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

	@CompileStatic
	@PackageScope
	trait Processable {

		private Object processedObject

		@PackageScope
		<T> void runProcessor(CompilationInfoProcessor<T> processor) {
			this.processedObject = processor.process(this as CompilationInfo)
		}

		def <T> T getProcessedObject(Class<T> clazz) {
			if (processedObject.getClass() == clazz) {
				return processedObject as T
			}
			throw new IllegalStateException("Processor is either not set or not the provided type!")
		}
	}
}
