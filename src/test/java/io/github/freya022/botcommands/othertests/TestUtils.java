/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package io.github.freya022.botcommands.othertests;

public class TestUtils {
	@SuppressWarnings("SameParameterValue")
	public static double measureTime(String desc, int warmup, int iterations, Runnable code) {
		for (int i = 0; i < warmup; i++) {
			code.run();
		}

		long worst = Long.MIN_VALUE;
		long best = Long.MAX_VALUE;
		long total = 0;
		for (int i = 0; i < iterations; i++) {
			final long start = System.nanoTime();

			code.run();

			final long end = System.nanoTime();

			final long elapsed = end - start;
			worst = Math.max(worst, elapsed);
			best = Math.min(best, elapsed);
			total += elapsed;
		}

		double average = total / 1_000_000.0 / iterations;

		System.out.printf(desc + " : Iterations : %s, Best : %.7f ms, Worst : %.7f ms, Average : %.7f ms, Total : %.7f ms%n", iterations, best / 1_000_000.0, worst / 1_000_000.0, average, total / 1_000_000.0);

		return average;
	}
}
