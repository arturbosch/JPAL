package io.gitlab.arturbosch.jpal.core

import io.gitlab.arturbosch.jpal.ast.TypeHelper
import io.gitlab.arturbosch.jpal.internal.PrefixTree

/**
 * @author Artur Bosch
 */
final class PackageHelper {

	private PackageHelper() {}

	static String determineRootPackageName(List<String> packages) {

		if (packages.isEmpty()) {
			return TypeHelper.DEFAULT_PACKAGE
		}
		if (packages.size() == 1) {
			return packages[0]
		}

		def tree = new PrefixTree()

		packages.each { tree.insertWord(it) }
		def longestCommonPrefix = tree.dfs()

		def shortestCommonPackage = longestCommonPrefix
		for (String packageName : packages) {
			if (shortestCommonPackage.startsWith(packageName)) {
				shortestCommonPackage = packageName
			}
		}

		if (shortestCommonPackage != "" && shortestCommonPackage != ".") {
			def lastIndex = shortestCommonPackage.length() - 1
			if (shortestCommonPackage.charAt(lastIndex) == '.' as char) {
				return shortestCommonPackage.substring(0, lastIndex)
			} else {
				return shortestCommonPackage
			}
		}

		return TypeHelper.DEFAULT_PACKAGE
	}
}
