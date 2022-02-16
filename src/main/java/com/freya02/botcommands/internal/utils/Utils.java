package com.freya02.botcommands.internal.utils;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.Logging;
import com.freya02.botcommands.api.components.ComponentManager;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public final class Utils {
	@SuppressWarnings("SpellCheckingInspection")
	private static final char[] chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ!\"#$%&'()*+,-./:;<=>?_^[]{}|".toCharArray();

	private static final Logger LOGGER = Logging.getLogger();

	@Contract("null, _ -> fail")
	@NotNull
	public static String requireNonBlank(String str, String name) {
		if (str == null) {
			throw new IllegalArgumentException(name + " may not be null");
		} else if (str.isBlank()) {
			throw new IllegalArgumentException(name + " may not be blank");
		}

		return str;
	}

	public static ExecutorService createCommandPool(ThreadFactory factory) {
		return new ThreadPoolExecutor(4, Runtime.getRuntime().availableProcessors() * 4, //*4 considering there should not be cpu intensive tasks but may be tasks sleeping
				60, TimeUnit.SECONDS,
				new LinkedBlockingQueue<>(), factory);
	}

	public static void printExceptionString(String message, Throwable e) {
		final CharArrayWriter out = new CharArrayWriter(1024);
		out.append(message).append("\n");
		final PrintWriter printWriter = new PrintWriter(out);
		e.printStackTrace(printWriter);

		LOGGER.error(out.toString());
	}

	/**
	 * Returns the deepest cause of this throwable
	 */
	@NotNull
	public static Throwable getException(Throwable e) {
		while (e.getCause() != null) {
			e = e.getCause();
		}

		return e;
	}

	public static ErrorResponseException getErrorResponseException(Throwable e) {
		do {
			if (e instanceof ErrorResponseException ex) {
				return ex;
			}

			e = e.getCause();
		} while (e != null);

		return null;
	}

	public static String randomId(int n) {
		final ThreadLocalRandom random = ThreadLocalRandom.current();

		final StringBuilder sb = new StringBuilder(64);
		for (int i = 0; i < n; i++) {
			sb.append(chars[random.nextInt(0, chars.length)]);
		}

		return sb.toString();
	}

	@NotNull
	public static ComponentManager getComponentManager(BContext context) {
		if (context == null)
			throw new IllegalStateException("The ComponentManager must be set in CommandsBuilder in order to use components (no BContext so assuming it didn't get set)");

		final ComponentManager componentManager = context.getComponentManager();
		if (componentManager == null)
			throw new IllegalStateException("The ComponentManager must be set in CommandsBuilder in order to use components");

		return componentManager;
	}

	public static Class<?> getBoxedType(Class<?> type) {
		if (type.isPrimitive()) {
			if (type == boolean.class) {
				return Boolean.class;
			} else if (type == double.class) {
				return Double.class;
			} else if (type == long.class) {
				return Long.class;
			} else if (type == int.class) {
				return Integer.class;
			} else if (type == float.class) {
				return Float.class;
			} else if (type == byte.class) {
				return Byte.class;
			} else if (type == char.class) {
				return Character.class;
			} else {
				LOGGER.error("Cannot box type {}", type.getName());

				return type;
			}
		} else {
			return type;
		}
	}

	@NotNull
	public static String formatMethodShort(@NotNull Method method) {
		return method.getDeclaringClass().getSimpleName()
				+ "#"
				+ method.getName()
				+ Arrays.stream(method.getParameterTypes())
				.map(Class::getSimpleName)
				.collect(Collectors.joining(", ", "(", ")"));
	}

	public static String readResource(String url) {
		final Class<?> callerClass = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass();
		try (InputStream stream = callerClass.getResourceAsStream(url)) {
			if (stream == null) throw new RuntimeException("Unable to read resource at " + url + " for class " + callerClass.getName());

			return new String(stream.readAllBytes());
		} catch (IOException e) {
			throw new RuntimeException("Unable to read resource at " + url + " for class " + callerClass.getName(), e);
		}
	}

	//TODO move to StringUtils
	public static boolean startsWithIgnoreCase(@NotNull String original, @NotNull String input) {
		return original.regionMatches(true, 0, input, 0, input.length());
	}
}