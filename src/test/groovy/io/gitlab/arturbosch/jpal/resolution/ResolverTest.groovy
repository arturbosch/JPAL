package io.gitlab.arturbosch.jpal.resolution

import com.github.javaparser.ast.expr.SimpleName
import com.github.javaparser.ast.type.ClassOrInterfaceType
import io.gitlab.arturbosch.jpal.Helper
import io.gitlab.arturbosch.jpal.core.JPAL
import spock.lang.Specification

/**
 * @author Artur Bosch
 */
class ResolverTest extends Specification {

	private path = Helper.BASE_PATH.resolve("resolving/SolveTypeDummy.java")
	private storage = JPAL.new(path)
	private info = storage.getCompilationInfo(path).get()
	private resolver = new Resolver(storage)

	def "Resolve"() {
		when: "resolving a type"
		def type = resolver.resolveType(new ClassOrInterfaceType("SolveTypeDummy"), info)
		then: "its the qualified type of dummy"
		type.get().name == "io.gitlab.arturbosch.jpal.dummies.resolving.SolveTypeDummy"
	}

	def "ResolveType"() {
		given: "all symbols of dummy class"
		def symbols = info.unit.getNodesByType(SimpleName)
		when: "resolving the symbols"
		def references = symbols.collect { resolver.resolve(it, info) }
		then: "all must be resolved"
		references.stream().allMatch { it.isPresent() }
	}

}
