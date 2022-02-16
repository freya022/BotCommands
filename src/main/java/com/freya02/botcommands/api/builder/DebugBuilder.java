package com.freya02.botcommands.api.builder;

public class DebugBuilder {
	private static boolean logApplicationDiffs;

	/**
	 * Sets whether the differences between old and new application commands data should be logged
	 *
	 * @param logApplicationDiffs <code>true</code> if the differences should be logged
	 */
	public void setLogApplicationDiffs(boolean logApplicationDiffs) {
		DebugBuilder.logApplicationDiffs = logApplicationDiffs;
	}

	public static boolean isLogApplicationDiffsEnabled() {
		return logApplicationDiffs;
	}
}
