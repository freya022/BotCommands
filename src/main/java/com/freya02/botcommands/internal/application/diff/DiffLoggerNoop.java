package com.freya02.botcommands.internal.application.diff;

public class DiffLoggerNoop implements DiffLogger {
	@Override
	public void trace(int indent, String formatStr, Object... objects) {}

	@Override
	public void printLogs() {}
}
