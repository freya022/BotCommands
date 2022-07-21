package com.freya02.botcommands.api.builder;

public class DebugBuilder {
	private static boolean logApplicationDiffs;
	private static boolean logMissingLocalization;

	public static boolean isLogMissingLocalizationEnabled() {
		return logMissingLocalization;
	}

	/**
	 * Sets whether the differences between old and new application commands data should be logged
	 *
	 * @param logApplicationDiffs <code>true</code> if the differences should be logged
	 *
	 * @return This builder for chaining convenience
	 */
	public DebugBuilder setLogApplicationDiffs(boolean logApplicationDiffs) {
		DebugBuilder.logApplicationDiffs = logApplicationDiffs;

		return this;
	}

	/**
	 * Sets whether the missing localization strings when creation the command objects should be logged
	 *
	 * @param logMissingLocalization <code>true</code> if the missing localization strings should be logged
	 *
	 * @return This builder for chaining convenience
	 */
	public DebugBuilder setLogMissingLocalization(boolean logMissingLocalization) {
		DebugBuilder.logMissingLocalization = logMissingLocalization;

		return this;
	}

	public static boolean isLogApplicationDiffsEnabled() {
		return logApplicationDiffs;
	}
}
