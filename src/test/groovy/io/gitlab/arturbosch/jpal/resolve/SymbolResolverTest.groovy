package io.gitlab.arturbosch.jpal.resolve

import com.github.javaparser.ast.expr.SimpleName
import com.github.javaparser.ast.expr.VariableDeclarationExpr
import io.gitlab.arturbosch.jpal.Helper
import spock.lang.Specification

/**
 * @author Artur Bosch
 */
class SymbolResolverTest extends Specification {

	def "resolve"() {
		given: "symbols of method m2 of ResolvingDummy"
		def path = Helper.BASE_PATH.resolve("ResolvingDummy.java")
		def unit = Helper.compile(path)
		def resolutionData = ResolutionData.of(unit)
		def method = Helper.nth(unit, 2)
		def symbols = method.body.get().getNodesByType(SimpleName.class)
		println symbols

		when: "resolving all symbols"
		def resolvedSymbols = symbols.collect { Resolver.resolveSymbol(it, resolutionData) }
		println resolvedSymbols

		then: "one SymbolReference must be a field"
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
		def resolvedSymbols = symbols.collect { Resolver.resolveSymbol(it, resolutionData) }

		then: "one SymbolReference must be a field"
		resolvedSymbols.find { it.get().isField() }
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
		def expectedQualifiedType = Resolver.getQualifiedType(resolutionData, typeOfC)

		when: "trying to resolve symbol"
		def resolvedTypeOfA = Resolver.resolveSymbol(ifA, resolutionData)
		def resolvedTypeOfB = Resolver.resolveSymbol(ifB, resolutionData)
		def resolvedTypeOfC = Resolver.resolveSymbol(ifC, resolutionData)
		def resolvedTypeInsideIfBlockOfC = Resolver.resolveSymbol(ifCagain, resolutionData)
		def resolvedTypeInReturnOfB = Resolver.resolveSymbol(returnB, resolutionData)
		def resolvedTypeInReturnOfD = Resolver.resolveSymbol(returnD, resolutionData)
		def resolvedTypeInMethodOfC = Resolver.resolveSymbolInMethod(ifC, method, resolutionData)
		def resolvedTypeInMethodOfD = Resolver.resolveSymbolInParameters(returnD, method, resolutionData)

		then: "getting right QualifiedType of type int"
		resolvedTypeOfA.get().isField()
		resolvedTypeInMethodOfD.get().isParameter()
		resolvedTypeOfC.get().isLocaleVariable()

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
