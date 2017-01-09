package io.gitlab.arturbosch.jpal.resolution

import com.github.javaparser.ast.expr.SimpleName
import io.gitlab.arturbosch.jpal.Helper
import io.gitlab.arturbosch.jpal.core.JPAL
import spock.lang.Specification

/**
 * @author Artur Bosch
 */
class SymbolSolverTest extends Specification {

	def "ResolutionDummy - method 1 - all variables"() {
		given: "compilation info for a class"
		def storage = JPAL.new(Helper.BASE_PATH)
		def solver = new SymbolSolver(storage)
		def info = storage.getCompilationInfo(Helper.RESOLVING_DUMMY).get()
		def symbols = Helper.nth(info.unit, 0).body.get().getNodesByType(SimpleName.class)

		when: "resolving all symbols"
		def symbolReferences = symbols.collect { solver.resolve(it, info).get() }

		then: "all symbols are resolved to INT - JavaReference"
		symbolReferences.each {
			assert it.qualifiedType.isPrimitive()
		}
	}

	def "ResolutionDummy - method 2 - all variables same name with this"() {
		given: "compilation info for a class"
		def storage = JPAL.new(Helper.BASE_PATH)
		def solver = new SymbolSolver(storage)
		def info = storage.getCompilationInfo(Helper.RESOLVING_DUMMY).get()
		def symbols = Helper.nth(info.unit, 1).body.get().getNodesByType(SimpleName.class)

		when: "resolving all symbols"
		def symbolReferences = symbols.collect { solver.resolve(it, info).get() }

		then: "this.x must be a field, all others local"
		symbolReferences.stream().filter { it.asVariable().isField() }.collect().size() == 1
		symbolReferences.stream().filter { !it.asVariable().isField() }.allMatch { it.asVariable().isLocaleVariable() }
	}

	def "ResolutionDummy - method 3 - method calls and field accesses, no chains"() {
		given: "compilation info for a class"
		def storage = JPAL.new(Helper.BASE_PATH)
		def solver = new SymbolSolver(storage)
		def info = storage.getCompilationInfo(Helper.RESOLVING_DUMMY).get()
		def symbols = Helper.nth(info.unit, 2).body.get().getNodesByType(SimpleName.class)

		when: "resolving all symbols"
		def symbolReferences = symbols.collect { solver.resolve(it, info).get() }

		then: "it must resolve three methods and six fields"
		symbolReferences.stream().filter { it.isMethod() }.collect().size() == 3
		symbolReferences.stream().filter { it.isVariable() }
				.map { it.asVariable() }
				.filter { it.isField() }.collect().size() == 6
		symbolReferences.stream().filter { it.isVariable() }
				.map { it.asVariable() }
				.filter { it.isLocaleVariable() }.collect().size() == 3
	}

	def "ResolutionDummy - method 4 - method/field chaining"() {
		given: "compilation info for a class"
		def storage = JPAL.new(Helper.BASE_PATH)
		def solver = new SymbolSolver(storage)
		def info = storage.getCompilationInfo(Helper.RESOLVING_DUMMY).get()
		def symbols = Helper.nth(info.unit, 3).body.get().getNodesByType(SimpleName.class)
		symbols.each { println it }

		when: "resolving all symbols"
		def symbolReferences = symbols.collect { solver.resolve(it, info) }
		symbolReferences.each { println it }

		then: "this.x must be a field, all others local"
		symbolReferences.find { it.isPresent() }
//		symbolReferences.stream().filter { it.isMethod() }.collect().size() == 3
	}

}
