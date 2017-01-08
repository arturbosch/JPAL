package io.gitlab.arturbosch.jpal.resolve

import com.github.javaparser.ast.expr.SimpleName
import com.github.javaparser.ast.expr.VariableDeclarationExpr
import io.gitlab.arturbosch.jpal.Helper
import io.gitlab.arturbosch.jpal.core.CompilationStorage
import io.gitlab.arturbosch.jpal.core.JPAL
import spock.lang.Specification

/**
 * @author Artur Bosch
 */
class SymbolResolverTest extends Specification {

	CompilationStorage storage
	Resolver resolver

	def setup() {
		storage = JPAL.new(Helper.BASE_PATH)
		resolver = new Resolver(storage)
	}

	def "resolve"() {
		given: "symbols of method m2 of ResolvingDummy"
		def path = Helper.BASE_PATH.resolve("ResolvingDummy.java")
		def unit = Helper.compile(path)
		def resolutionData = ResolutionData.of(unit)
		def method = Helper.nth(unit, 2)
		def symbols = method.body.get().getNodesByType(SimpleName.class)
		println symbols

		when: "resolving all symbols"
		def resolvedSymbols = symbols.collect { resolver.resolveSymbol(it, resolutionData) }
		resolvedSymbols.each { println it }

		then: "resolve method call and field access"
		true
	}

	def "resolve variables all with name x"() {
		given: "symbols of method m2 of ResolvingDummy"
		def path = Helper.BASE_PATH.resolve("ResolvingDummy.java")
		def unit = Helper.compile(path)
		def resolutionData = ResolutionData.of(unit)
		def method = Helper.nth(unit, 1)
		def symbols = method.body.get().getNodesByType(SimpleName.class)

		when: "resolving all symbols"
		def resolvedSymbols = symbols.collect { resolver.resolveSymbol(it, resolutionData) }

		then: "one SymbolReference must be a field"
		resolvedSymbols.stream()
				.map { it.get() }
				.filter { it.isVariable() }
				.map { it.asVariable() }
				.filter { it.isField() }.find()
	}

	def "resolve variables domain test"() {
		given: "a method with variable references"
		def path = Helper.BASE_PATH.resolve("ResolvingDummy.java")
		def unit = Helper.compile(path)
		def resolutionData = ResolutionData.of(unit)
		def method = Helper.nth(unit, 0)

		// variables to test
		def blockStmt = method.body.get()
		def assignStmt = blockStmt.getStatement(0)
		def ifStmt = blockStmt.getStatement(1).getNodesByType(SimpleName.class)
		def returnStmt = blockStmt.getStatement(2).getNodesByType(SimpleName.class)
		def ifA = ifStmt[0]
		def ifC = ifStmt[1]
		def ifB = ifStmt[2]
		def ifCagain = ifStmt[3]
		def returnB = returnStmt[0]
		def returnD = returnStmt[1]

		// expected type is QualifiedType(INT, PRIMITIVE) for all tested variables
		def typeOfC = (assignStmt.childNodes[0] as VariableDeclarationExpr).commonType
		def expectedQualifiedType = resolver.getQualifiedType(resolutionData, typeOfC)

		when: "trying to resolve symbol"
		def resolvedTypeOfA = resolver.resolveSymbol(ifA, resolutionData)
		def resolvedTypeOfB = resolver.resolveSymbol(ifB, resolutionData)
		def resolvedTypeOfC = resolver.resolveSymbol(ifC, resolutionData)
		def resolvedTypeInsideIfBlockOfC = resolver.resolveSymbol(ifCagain, resolutionData)
		def resolvedTypeInReturnOfB = resolver.resolveSymbol(returnB, resolutionData)
		def resolvedTypeInReturnOfD = resolver.resolveSymbol(returnD, resolutionData)
		def resolvedTypeInMethodOfC = resolver.resolveSymbolInMethod(ifC, method, resolutionData)
		def resolvedTypeInMethodOfD = resolver.resolveSymbolInParameters(returnD, method, resolutionData)

		then: "getting right QualifiedType of type int"
		resolvedTypeOfA.get().asVariable().isField()
		resolvedTypeInMethodOfD.get().isParameter()
		resolvedTypeOfC.get().asVariable().isLocaleVariable()

		resolvedTypeOfA.get().qualifiedType == expectedQualifiedType
		resolvedTypeOfB.get().qualifiedType == expectedQualifiedType
		resolvedTypeOfC.get().qualifiedType == expectedQualifiedType
		resolvedTypeInsideIfBlockOfC.get().qualifiedType == expectedQualifiedType
		resolvedTypeInReturnOfB.get().qualifiedType == expectedQualifiedType
		resolvedTypeInReturnOfD.get().qualifiedType == expectedQualifiedType
		resolvedTypeInMethodOfC.get().qualifiedType == expectedQualifiedType
		resolvedTypeInMethodOfD.get().qualifiedType == expectedQualifiedType
	}

}
