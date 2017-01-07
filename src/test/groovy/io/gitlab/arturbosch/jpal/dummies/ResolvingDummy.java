package io.gitlab.arturbosch.jpal.dummies;

import io.gitlab.arturbosch.jpal.dummies.resolving.SolveTypeDummy;

/**
 * @author Artur Bosch
 */
@SuppressWarnings("ALL")
public class ResolvingDummy {

	private int a = 5;
	private int b = 5;

	private int x = 5;

	private InnerResolvingDummy inner = new InnerResolvingDummy();
	private SolveTypeDummy solveDummy = new SolveTypeDummy();

	public int m(int d) {
		int c = 5;
		if (a == c) {
			b += c;
		}
		return b + d;
	}

	public void m2() {
		int x = 0;
		while (true) {
			this.x = x + 1;
			int xnew = x + 2;
		}
	}

	public String m3() {
		String method = solveDummy.method();
		return inner.s + method;
	}

	class InnerResolvingDummy {
		String s;
	}
}
