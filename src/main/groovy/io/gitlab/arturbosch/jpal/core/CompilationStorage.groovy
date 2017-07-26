package io.gitlab.arturbosch.jpal.core

import io.gitlab.arturbosch.jpal.resolution.QualifiedType

import java.nio.file.Path

/**
 * @author Artur Bosch
 */
interface CompilationStorage {

	/**
	 * @return true if root package is the default package 'default.'
	 */
	boolean isDefaultPackageRootPackage()

	/**
	 * @return the root package determined by all compiled units
	 */
	String getRootPackageName()

	/**
	 * @return retrieves all stored qualified type keys
	 */
	Set<QualifiedType> getAllQualifiedTypes()

	/**
	 * @return retrieves all stores compilation info's
	 */
	Collection<CompilationInfo> getAllCompilationInfo()

	/**
	 * Maybe a compilation unit for given path is found.
	 *
	 * @param path path for which the info is asked
	 * @return optional of compilation unit
	 */
	Optional<CompilationInfo> getCompilationInfo(Path path)

	/**
	 * Maybe a compilation unit for given qualified type is found.
	 *
	 * @param qualifiedType type for which the info is asked
	 * @return optional of compilation unit
	 */
	Optional<CompilationInfo> getCompilationInfo(QualifiedType qualifiedType)


}
