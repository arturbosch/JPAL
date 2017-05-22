package io.gitlab.arturbosch.jpal.ast

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.type.ClassOrInterfaceType
import groovy.transform.CompileStatic
import io.gitlab.arturbosch.jpal.core.CompilationStorage
import io.gitlab.arturbosch.jpal.resolution.QualifiedType
import io.gitlab.arturbosch.jpal.resolution.ResolutionData
import io.gitlab.arturbosch.jpal.resolution.Resolver

/**
 * Resolves and collects recursively all qualified types of the sub classes of starting class.
 *
 * @author Artur Bosch
 */
@CompileStatic
class AncestorCollector {

	private Resolver typeSolver
	private CompilationStorage storage

	AncestorCollector(Resolver resolver) {
		typeSolver = resolver
		storage = resolver.storage
	}

	Set<QualifiedType> getAll(ResolutionData startData, ClassOrInterfaceDeclaration aClass) {
		def types = new HashSet<QualifiedType>()
		resolve(types, startData, aClass)
		return types
	}

	private void resolve(Set<QualifiedType> resolved, ResolutionData data, ClassOrInterfaceDeclaration aClass) {
		def types = aClass.implementedTypes + aClass.extendedTypes
		def ancestorTypes = resolveAncestorTypes(data, types)
		def ancestors = extractAncestorClasses(ancestorTypes)

		resolved.addAll(ancestorTypes)
		ancestors.each { resolve(resolved, it.value, it.key) }
	}

	private List<QualifiedType> resolveAncestorTypes(ResolutionData data,
													 List<ClassOrInterfaceType> types) {
		return types.collect { typeSolver.resolveType(it, data) }
	}

	private Map<ClassOrInterfaceDeclaration, ResolutionData> extractAncestorClasses(List<QualifiedType> types) {
		def map = new HashMap<ClassOrInterfaceDeclaration, ResolutionData>()

		for (type in types) {
			storage.getCompilationInfo(type).ifPresent {
				def declaration = it.getTypeDeclarationByQualifier(type).orElse(null)
				if (declaration && declaration instanceof ClassOrInterfaceDeclaration) {
					map.put(declaration as ClassOrInterfaceDeclaration, it.data)
				}
			}
		}

		return map
	}

}
