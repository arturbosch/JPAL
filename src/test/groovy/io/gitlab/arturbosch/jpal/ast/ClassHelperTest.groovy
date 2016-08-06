package io.gitlab.arturbosch.jpal.ast

import io.gitlab.arturbosch.jpal.Helper
import spock.lang.Specification

/**
 * @author artur
 */
class ClassHelperTest extends Specification {

	def "class is empty and has no methods"() {
		expect:
		ClassHelper.hasNoMethods(aClass)
		ClassHelper.isEmptyBody(aClass)
		where:
		aClass = Helper.firstClass(Helper.compile(Helper.EMPTY_DUMMY))
	}

	def "method is within class scope"() {
		expect:
		ClassHelper.inClassScope(aMethod, aClass.name)
		where:
		unit = Helper.compile(Helper.CYCLE_DUMMY)
		aClass = Helper.firstClass(unit)
		aMethod = Helper.nth(unit, 0)
	}
}
