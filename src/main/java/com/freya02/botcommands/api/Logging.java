package com.freya02.botcommands.api;

import net.dv8tion.jda.internal.utils.JDALogger;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

/**
 * Static methods which returns a {@link Logger} for the current class (the class which calls these methods)
 * <br>The logger is implemented by the implementation you provide, such as logback-classic, or fallback to the default JDA logger
 */
public class Logging {
	private static final StackWalker WALKER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);

	/**
	 * Returns the {@link Logger} for the class which calls this method
	 *
	 * @return The {@link Logger} for the class which calls this method
	 */
	@NotNull
	public static Logger getLogger() {
		return JDALogger.getLog(WALKER.getCallerClass());
	}

	/**
	 * Returns the {@link Logger} for the class of this object
	 * <br>This might be useful to get a logger which targets an implementation class instead of a superclass
	 *
	 * @return The {@link Logger} for the class of this object
	 */
	@NotNull
	public static Logger getLogger(@NotNull Object obj) {
		return JDALogger.getLog(obj.getClass());
	}
}