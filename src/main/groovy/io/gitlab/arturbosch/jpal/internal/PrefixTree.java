package io.gitlab.arturbosch.jpal.internal;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Artur Bosch
 */
public class PrefixTree {

	private TrieNode root = new TrieNode('\0', null);

	public void insertWord(String word) {
		int length = word.length();
		char[] letters = word.toCharArray();
		TrieNode currentNode = root;
		for (int i = 0; i < length; i++) {
			char currentLetter = letters[i];
			if (!currentNode.children.containsKey(currentLetter))
				currentNode.children.put(currentLetter, new TrieNode(currentLetter, currentNode));
			currentNode = currentNode.children.get(currentLetter);
		}
		currentNode.fullWord = true;
	}

	public String dfs() {
		TrieSearch trieSearch = new TrieSearch();
		trieSearch.dfs(root, 0);
		TrieNode maxNode = trieSearch.maxNode;
		if (maxNode != null && maxNode.parent != null) {
			StringBuilder result = new StringBuilder();
			do {
				result.append(maxNode.letter);
				maxNode = maxNode.parent;
			} while (maxNode.parent != null);
			return result.reverse().toString();
		} else {
			return "";
		}
	}

	class TrieSearch {
		int maxDepth = -1;
		TrieNode maxNode = null;

		private void dfs(TrieNode node, int depth) {
			if (node.children.size() > 1 && depth > maxDepth) {
				maxDepth = depth;
				maxNode = node;
			}
			for (TrieNode current : node.children.values())
				dfs(current, depth + 1);
		}
	}

	class TrieNode {

		TrieNode parent;
		char letter;
		Map<Character, TrieNode> children;
		boolean fullWord;

		TrieNode(char letter, TrieNode parent) {
			this.parent = parent;
			this.letter = letter;
			this.children = new HashMap<>();
			this.fullWord = false;
		}

		@Override
		public String toString() {
			return "TrieNode{" +
					"parent=" + (parent == null ? "null" : parent.letter) +
					", letter=" + letter +
					", children=" + children.size() +
					", fullWord=" + fullWord +
					'}';
		}
	}
}
