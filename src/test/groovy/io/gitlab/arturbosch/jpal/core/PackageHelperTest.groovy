package io.gitlab.arturbosch.jpal.core

import io.gitlab.arturbosch.jpal.ast.TypeHelper
import spock.lang.Specification

/**
 * @author Artur Bosch
 */
class PackageHelperTest extends Specification {

	def "no packages"() {
		when: "determine root package with empty list"
		def packageName = PackageHelper.determineRootPackageName([])
		then: "default package name is chosen"
		packageName == TypeHelper.DEFAULT_PACKAGE
	}

	def "single package is root package"() {
		expect:
		packageName == "io.gitlab.arturbosch"
		where:
		packageName = PackageHelper.determineRootPackageName(["io.gitlab.arturbosch"])
	}

	def "shortest package prefix found"() {
		expect:
		packageName == "io.gitlab.arturbosch"
		where:
		packageName = PackageHelper.determineRootPackageName(
				["io.gitlab.arturbosch", "io.gitlab.arturbosch.jpal", "io.gitlab.arturbosch.javaparser"]
		)
	}

	def "shortest package prefix found even with trailing dots"() {
		expect:
		packageName == "io.gitlab.arturbosch"
		where:
		packageName = PackageHelper.determineRootPackageName(
				["io.gitlab.arturbosch.", "io.gitlab.arturbosch.jpal.", "io.gitlab.arturbosch.javaparser."]
		)
	}


	def "shortest package prefix for same names"() {
		expect:
		packageName == "io.gitlab.arturbosch"
		where:
		packageName = PackageHelper.determineRootPackageName(
				["io.gitlab.arturbosch", "io.gitlab.arturbosch.jpal"]
		)
	}
}
