package io.gitlab.arturbosch.jpal.ast

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import io.gitlab.arturbosch.jpal.Helper
import io.gitlab.arturbosch.jpal.core.JPAL
import io.gitlab.arturbosch.jpal.resolution.Resolver
import spock.lang.Specification

/**
 * @author Artur Bosch
 */
class TypeHelperAncestorTest extends Specification {

	private path = Helper.BASE_PATH.resolve("resolving/AncestorResolveType.java")
	private storage = JPAL.new(Helper.BASE_PATH.resolve("resolving"))
	private info = storage.getCompilationInfo(path).get()
	private resolver = new Resolver(storage)

	def "find all ancestors of a class"() {
		given: "a class with ancestors"
		def clazz = info.mainType as ClassOrInterfaceDeclaration
		when: "searching for ancestors"
		def qualifiedTypes = TypeHelper.findAllAncestors(clazz, resolver)
		then: "three ancestors are found"
		qualifiedTypes.size() == 5
		qualifiedTypes.find { it.shortName == "SubSolveTypeDummy" }
		qualifiedTypes.find { it.shortName == "SolveType" }
		qualifiedTypes.find { it.shortName == "SolveTypeDummy" }
		qualifiedTypes.find { it.shortName == "Ancestor" }
		qualifiedTypes.find { it.shortName == "SubAncestorType" }
	}
}
