package io.gitlab.arturbosch.jpal.core

/**
 * @author Artur Bosch
 */
interface CompilationInfoProcessor<T> {
	T process(CompilationInfo info)
}