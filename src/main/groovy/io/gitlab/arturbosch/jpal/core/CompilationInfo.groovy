package io.gitlab.arturbosch.jpal.core

import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.TypeDeclaration
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import io.gitlab.arturbosch.jpal.ast.TypeHelper
import io.gitlab.arturbosch.jpal.internal.Validate
import io.gitlab.arturbosch.jpal.resolution.QualifiedType
import io.gitlab.arturbosch.jpal.resolution.ResolutionData
import io.gitlab.arturbosch.jpal.resolution.solvers.TypeSolver

import java.nio.file.Path
import java.nio.file.Paths

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
	final Path relativePath
	final TypeDeclaration mainType
	final ResolutionData data
	final Map<QualifiedType, TypeDeclaration> innerClasses

	/**
	 * Are set after creation of all compilation units as after that point only
	 * is resolution of star imports possible.
	 */
	private List<QualifiedType> usedTypes

	List<QualifiedType> getUsedTypes() {
		return usedTypes
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
		def mainClassAndInnerClassesPair = TypeHelper.getQualifiedDeclarationsOfInnerClasses(unit)
		QualifiedType qualifiedType = mainClassAndInnerClassesPair.a.a
		TypeDeclaration mainType = mainClassAndInnerClassesPair.a.b
		Map<QualifiedType, TypeDeclaration> innerTypes = mainClassAndInnerClassesPair.b
		return new CompilationInfo(qualifiedType, mainType, innerTypes, unit, path)
	}

	private CompilationInfo(QualifiedType qualifiedType,
							TypeDeclaration mainType,
							Map<QualifiedType, TypeDeclaration> innerClasses,
							CompilationUnit unit,
							Path path) {

		this.innerClasses = innerClasses
		this.mainType = mainType
		this.qualifiedType = qualifiedType
		this.unit = unit
		this.path = path
		this.relativePath = relativizePath(path, unit)
		this.usedTypes = Collections.emptyList()
		this.data = ResolutionData.of(unit)
	}

	private static void relativizePath(Path path, CompilationUnit unit) {
		def name = path.fileName
		unit.packageDeclaration.ifPresent { name = Paths.get(it.nameAsString.replace(".", "//")).resolve(name) }
		name
	}

	@PackageScope
	void findUsedTypes(TypeSolver typeSolver) {
		Validate.notNull(usedTypes)
		def usedTypes = TypeHelper.findAllUsedTypes(unit, typeSolver)
		this.usedTypes = replaceQualifiedTypesOfInnerClasses(usedTypes, innerClasses.keySet())
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
