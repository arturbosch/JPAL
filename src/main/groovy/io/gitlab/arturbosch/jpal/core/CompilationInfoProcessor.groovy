package io.gitlab.arturbosch.jpal.core

import io.gitlab.arturbosch.jpal.resolution.Resolver

/**
 * @author Artur Bosch
 */
interface CompilationInfoProcessor {
	void process(CompilationInfo info, Resolver resolver)
}
