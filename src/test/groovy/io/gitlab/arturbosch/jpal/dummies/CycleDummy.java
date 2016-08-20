package io.gitlab.arturbosch.jpal.dummies;

import io.gitlab.arturbosch.jpal.Helper;
import io.gitlab.arturbosch.jpal.dummies.test.*;

/**
 * Don't touch this class, tests depend on exact token structure!
 *
 * @author artur
 */
@SuppressWarnings("ALL")
class CycleDummy {

	public void compute() {
		Math.abs(100);
	}

	private void meah() {
		TestReference reference = new TestReference();
		Helper helper = new Helper();
	}

	class InnerCycleOne {
		InnerCycleTwo cycleTwo = new InnerCycleTwo();
	}

	class InnerCycleTwo {
		InnerCycleOne cycleOne = new InnerCycleOne();
	}
}
