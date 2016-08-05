package io.gitlab.arturbosch.japa.resolve

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

/**
 * Represent qualified types. Contains convenience methods to check if the underlining
 * type is a reference or primitive. Inner classes can also be represented.
 *
 * @author artur
 */
@ToString(includeNames = false, includePackage = false)
@EqualsAndHashCode
class QualifiedType {

	String name
	TypeToken typeToken

	/**
	 * Type of the type.
	 */
	enum TypeToken {
		PRIMITIVE, BOXED_PRIMITIVE, REFERENCE, JAVA_REFERENCE, UNKNOWN
	}

	QualifiedType(String name, TypeToken typeToken) {
		this.name = name
		this.typeToken = typeToken
	}

	boolean isPrimitive() {
		return typeToken == TypeToken.PRIMITIVE || typeToken == TypeToken.BOXED_PRIMITIVE
	}

	boolean isFromJdk() {
		return typeToken == TypeToken.JAVA_REFERENCE
	}

	boolean isReference() {
		return typeToken == TypeToken.REFERENCE
	}

	/**
	 * @return the class name
	 */
	String shortName() {
		def index = name.lastIndexOf(".")
		if (index == -1)
			return name
		return name.substring(index + 1)
	}

	/**
	 * Can be resolved with a project path to obtain the absolute path to an
	 * java class file.
	 *
	 * @return the package structure as a string
	 */
	String asStringPathToJavaFile() {
		def tmp = name
		if (isInnerClass()) {
			def lastIndexOf = name.lastIndexOf(".")
			tmp = name.substring(0, lastIndexOf)
		}
		return "${tmp.replaceAll("\\.", "/")}.java"
	}

	/**
	 * @return new qualified type if it represents an inner class
	 */
	QualifiedType asOuterClass() {
		if (isInnerClass()) {
			return new QualifiedType(name.substring(0, name.lastIndexOf(".")), typeToken)
		}
		return this
	}

	/**
	 * @return true if type is a inner class
	 */
	boolean isInnerClass() {
		def tokens = Arrays.asList(name.split("\\."))
		if (tokens.size() > 1) {
			def secondLastToken = tokens.get(tokens.size() - 2)
			return !secondLastToken.isEmpty() && Character.isUpperCase(secondLastToken.charAt(0))
		}
		return false
	}
}
