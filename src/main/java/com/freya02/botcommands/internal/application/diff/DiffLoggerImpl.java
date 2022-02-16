package com.freya02.botcommands.internal.application.diff;

import com.freya02.botcommands.api.Logging;
import org.slf4j.Logger;

import java.util.Stack;

public class DiffLoggerImpl implements DiffLogger {
	private static final Logger LOGGER = Logging.getLogger();

	private final Stack<LogItem> logItems = new Stack<>();

	@Override
	public void trace(int indent, String formatStr, Object... objects) {
		logItems.add(new LogItem(indent, formatStr, objects));
	}

	@Override
	public void printLogs() {
		while (!logItems.isEmpty()) {
			final LogItem item = logItems.pop();

			LOGGER.trace("{}{}", "\t".repeat(item.indent()), String.format(item.formatStr(), item.objects()));
		}
	}
}
