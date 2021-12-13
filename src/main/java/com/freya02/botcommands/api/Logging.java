package com.freya02.botcommands.api;

import net.dv8tion.jda.internal.utils.JDALogger;
import org.slf4j.Logger;

public class Logging {
	private static final StackWalker WALKER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);

	public static Logger getLogger() {
		return JDALogger.getLog(WALKER.getCallerClass());
	}

	public static Logger getLogger(Object obj) {
		return JDALogger.getLog(obj.getClass());
	}
}