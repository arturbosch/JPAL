package io.gitlab.arturbosch.jpal.resolve

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import io.gitlab.arturbosch.jpal.internal.Validate

/**
 * Represent qualified types. Contains convenience methods to check if the underlining
 * type is a reference or primitive. Inner classes can also be represented.
 *
 * @author artur
 */
@ToString(includeNames = false, includePackage = false, excludes = ["shortName", "onlyPackageName"])
@EqualsAndHashCode(excludes = ["shortName", "onlyPackageName"])
@CompileStatic
class QualifiedType {

	final String name
	final String shortName
	final String onlyPackageName
	final TypeToken typeToken

	/**
	 * Nature of the type. Should be self explained.
	 * Unknown types means that the resolver could not retrieve the qualified type.
	 */
	@CompileStatic
	enum TypeToken {
		PRIMITIVE, BOXED_PRIMITIVE, REFERENCE, JAVA_REFERENCE, UNKNOWN
	}

	QualifiedType(String name, TypeToken typeToken) {
		this.name = Validate.notNull(name)
		this.typeToken = Validate.notNull(typeToken)
		this.shortName = extractShortName(name)
		this.onlyPackageName = extractPackageName(name)
	}

	private static String extractShortName(String name) {
		def index = name.lastIndexOf(".")
		if (index == -1)
			return name
		return name.substring(index + 1)
	}

	private static String extractPackageName(String name) {
		return name.split("\\.").grep().takeWhile { Character.isLowerCase(it.charAt(0)) }.join(".")
	}

	/**
	 * @return true if primitive or boxed primitive
	 */
	boolean isPrimitive() {
		return typeToken == TypeToken.PRIMITIVE || typeToken == TypeToken.BOXED_PRIMITIVE
	}

	/**
	 * @return true if it's a jdk type - also see {@code JdkHelper}
	 */
	boolean isFromJdk() {
		return typeToken == TypeToken.JAVA_REFERENCE
	}

	/**
	 * @return true if reference type - this will be the most case where you want to operate on types
	 */
	boolean isReference() {
		return typeToken == TypeToken.REFERENCE
	}

	/**
	 * @return the class name without package structure
	 */
	String shortName() {
		return shortName
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
			return new QualifiedType(toOuterClass(), typeToken)
		}
		return this
	}

	private String toOuterClass() {
		def lastChunk = getLastChunk(name)
		def outerClassName = name.substring(0, name.lastIndexOf("."))
		def currentChunk = getLastChunk(outerClassName)
		while (!Character.isLowerCase(currentChunk.charAt(0))) {
			lastChunk = currentChunk
			outerClassName = outerClassName.substring(0, outerClassName.lastIndexOf("."))
			currentChunk = getLastChunk(outerClassName)
		}

		return "$outerClassName.$lastChunk"
	}

	private static String getLastChunk(String outerClassName) {
		outerClassName.substring(outerClassName.lastIndexOf('.') + 1)
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
