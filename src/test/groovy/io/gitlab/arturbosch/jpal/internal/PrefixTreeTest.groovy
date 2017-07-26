package io.gitlab.arturbosch.jpal.internal

import spock.lang.Specification

/**
 * @author Artur Bosch
 */
class PrefixTreeTest extends Specification {

	def "find common prefix"() {
		given: "a prefix tree"
		def trie = new PrefixTree()

		when: "inserting some words"
		trie.insertWord("hello")
		trie.insertWord("hallo")
		trie.insertWord("helooo")
		trie.insertWord("helo")
		trie.insertWord("helolo")
		trie.insertWord("helololo")

		then: "the common prefix is 'helo'"
		trie.dfs() == "helo"
	}

	def "only one word inserted"()  {
		given: "a prefix tree"
		def trie = new PrefixTree()

		when: "only one word inserted"
		trie.insertWord("io.gitlab.arturbosch.smartsmells.java")
		def commonPrefix = trie.dfs()

		then: "common prefix is empty"
		commonPrefix == ""
	}
}
