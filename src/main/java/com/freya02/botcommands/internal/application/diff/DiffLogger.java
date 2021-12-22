package com.freya02.botcommands.internal.application.diff;

public interface DiffLogger {
	boolean LOG_DIFFS = Boolean.parseBoolean(System.getProperty("botcommands.logDiff", "false"));

	static DiffLogger getLogger() {
		if (LOG_DIFFS) {
			return new DiffLoggerImpl();
		} else {
			return new DiffLoggerNoop();
		}
	}

	void trace(int indent, String formatStr, Object... objects);

	void printLogs();
}
