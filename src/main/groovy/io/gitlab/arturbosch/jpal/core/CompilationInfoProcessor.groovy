package io.gitlab.arturbosch.jpal.core

import io.gitlab.arturbosch.jpal.resolution.Resolver

/**
 * @author Artur Bosch
 */
interface CompilationInfoProcessor<T> {
	T process(CompilationInfo info, Resolver resolver)
}
