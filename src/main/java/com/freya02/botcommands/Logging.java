package com.freya02.botcommands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Logging {
	private static final StackWalker WALKER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);

	public static Logger getLogger() {
		return LoggerFactory.getLogger(WALKER.getCallerClass());
	}
}