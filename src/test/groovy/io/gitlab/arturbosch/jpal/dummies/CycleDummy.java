package io.gitlab.arturbosch.jpal.dummies;

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
	}

	class InnerCycleOne {
		InnerCycleTwo cycleTwo = new InnerCycleTwo();
	}

	class InnerCycleTwo {
		InnerCycleOne cycleOne = new InnerCycleOne();
	}
}
