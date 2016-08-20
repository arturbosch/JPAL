package io.gitlab.arturbosch.jpal.ast

import com.github.javaparser.ASTHelper
import com.github.javaparser.ast.body.MethodDeclaration
import io.gitlab.arturbosch.jpal.Helper
import spock.lang.Specification

/**
 * @author Artur Bosch
 */
class LocaleVariableHelperTest extends Specification {

	def "find locale variables"() {
		given: ""
		def unit = Helper.compile(Helper.DUMMY)
		def methods = ASTHelper.getNodesByType(unit, MethodDeclaration.class)
		when: "searching for locale variables within methods"
		def locales = LocaleVariableHelper.find(methods)
		then:
		locales.size() >= 3
	}
}
