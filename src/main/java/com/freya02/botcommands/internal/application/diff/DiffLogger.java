package com.freya02.botcommands.internal.application.diff;

import com.freya02.botcommands.api.builder.DebugBuilder;

public interface DiffLogger {
	static DiffLogger getLogger() {
		if (DebugBuilder.isLogApplicationDiffsEnabled()) {
			return new DiffLoggerImpl();
		} else {
			return new DiffLoggerNoop();
		}
	}

	void trace(int indent, String formatStr, Object... objects);

	void printLogs();
}
