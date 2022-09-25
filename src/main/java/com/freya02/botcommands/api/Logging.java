package com.freya02.botcommands.api;

import net.dv8tion.jda.internal.utils.JDALogger;
import org.slf4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Static methods which returns a {@link Logger} for the current class (the class which calls these methods)
 * <br>The logger is implemented by the implementation you provide, such as logback-classic, or fallback to the default JDA logger
 */
public class Logging {
	private static final StackWalker WALKER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
	private static final Map<Class<?>, Set<String>> loggedMap = Collections.synchronizedMap(new HashMap<>());

	/**
	 * Returns the {@link Logger} for the class which calls this method
	 *
	 * @return The {@link Logger} for the class which calls this method
	 */
	public static Logger getLogger() {
		return JDALogger.getLog(WALKER.getCallerClass());
	}

	/**
	 * Returns the {@link Logger} for the class of this object
	 * <br>This might be useful to get a logger which targets an implementation class instead of a superclass
	 *
	 * @return The {@link Logger} for the class of this object
	 */
	public static Logger getLogger(Object obj) {
		return JDALogger.getLog(obj.getClass());
	}

	public static boolean tryLog(Object... keyComponents) {
		return tryLog(WALKER.getCallerClass(), keyComponents);
	}

	public static boolean tryLog(Class<?> clazz, Object... keyComponents) {
		return loggedMap.computeIfAbsent(clazz, x -> new HashSet<>()).add(Arrays.stream(keyComponents).map(Object::toString).collect(Collectors.joining("/")));
	}

	public static void removeLogs(Class<?> clazz) {
		loggedMap.computeIfAbsent(clazz, x -> new HashSet<>()).clear();
	}

	public static void removeLogs() {
		removeLogs(WALKER.getCallerClass());
	}
}