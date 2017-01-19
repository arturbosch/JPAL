package io.gitlab.arturbosch.jpal.core

import spock.lang.Specification

/**
 * @author Artur Bosch
 */
class JPALTest extends Specification {

	def "updatable storage has no start info's"() {
		expect:
		storage.getAllCompilationInfo().isEmpty()
		where:
		storage = JPAL.updatable()
	}
}
