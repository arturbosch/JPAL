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

	// variable resolving
	public int m(int d) {
		int c = 5;
		if (a == c) {
			b += c;
		}
		return b + d;
	}

	// same symbol name in different variables
	public void m2() {
		int x = 0;
		while (true) {
			this.x = x + 1;
			int xnew = x + 2;
		}
	}

	// resolving 'this' or one level calls/accesses
	public String m3() {
		String method = solveDummy.method(x);
		m2();
		int x = this.x;
		return inner.s + method + inner.call();
	}

	// resolving method chaining + object creation
	public void m4() {
		new ChainResolving().inner.call();
	}

	class InnerResolvingDummy {
		String s;

		String call() {
			return "";
		}
	}

	class ChainResolving {
		private InnerResolvingDummy inner = new InnerResolvingDummy();
	}
}
