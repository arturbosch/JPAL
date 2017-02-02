package io.gitlab.arturbosch.jpal.dummies;

import java.util.List;

/**
 * @author Artur Bosch
 */
@SuppressWarnings("ALL")
public class ClassSignatureDummy {

	class VeryComplexInnerClass<T extends String, B extends List<T>>
			extends ClassSignatureDummy implements Cloneable, Runnable {

		@Override
		public void run() {

		}
	}
}
