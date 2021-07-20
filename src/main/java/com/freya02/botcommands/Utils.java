package com.freya02.botcommands;

import com.freya02.botcommands.annotation.ConditionalUse;
import com.freya02.botcommands.components.ComponentManager;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public final class Utils {
	@SuppressWarnings("SpellCheckingInspection")
	private static final char[] chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ!\"#$%&'()*+,-./:;<=>?_^[]{}|".toCharArray();

	private static final Logger LOGGER = Logging.getLogger();

	@SuppressWarnings("RedundantCast")
	static List<Class<?>> getClasses(Path jarPath, String packageName, int maxDepth) throws IOException {
		Path walkRoot = jarPath;
		final boolean isJar = IOUtils.getFileExtension(jarPath).equals("jar");
		if (isJar) {
			final FileSystem zfs = FileSystems.newFileSystem(jarPath, (ClassLoader) null);
			walkRoot = zfs.getPath("");
		}

		if (packageName != null) {
			walkRoot = walkRoot.resolve(packageName.replace(".", "\\"));
		}

		Path finalWalkRoot = walkRoot;
		return Files.walk(walkRoot, maxDepth).filter(p -> !Files.isDirectory(p)).filter(p -> IOUtils.getFileExtension(p).equals("class")).map(p -> {
			String result;

			if (isJar) {
				result = p.toString().replace('/', '.').substring(0, p.toString().length() - 6);
			} else {
				String relativePath = p.toString().replace(finalWalkRoot + "\\", "");
				result = relativePath.replace(".class", "").replace("\\", ".");

				if (packageName != null) {
					result = packageName + "." + result;
				}
			}

			try {
				return Class.forName(result, false, Utils.class.getClassLoader());
			} catch (ClassNotFoundException e) {
				LOGGER.error("Class not found: {}, is it in the class path ?", result);
				return null;
			}
		}).collect(Collectors.toList());
	}

	static String requireNonBlank(String str, String name) {
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

		if (LOGGER.isErrorEnabled()) {
			LOGGER.error(out.toString());
		} else {
			System.err.println(out);
		}
	}

	public static boolean isInstantiable(Class<?> aClass) throws IllegalAccessException, InvocationTargetException {
		boolean canInstantiate = true;
		for (Method declaredMethod : aClass.getDeclaredMethods()) {
			if (declaredMethod.isAnnotationPresent(ConditionalUse.class)) {
				if (Modifier.isStatic(declaredMethod.getModifiers())) {
					if (declaredMethod.getParameterCount() == 0 && declaredMethod.getReturnType() == boolean.class) {
						if (!declaredMethod.canAccess(null))
							throw new IllegalStateException("Method " + declaredMethod + " is not public");
						canInstantiate = (boolean) declaredMethod.invoke(null);
					} else {
						LOGGER.warn("Method {}#{} is annotated @ConditionalUse but does not have the correct signature (return boolean, no parameters)", aClass.getName(), declaredMethod.getName());
					}
				} else {
					LOGGER.warn("Method {}#{} is annotated @ConditionalUse but is not static", aClass.getName(), declaredMethod.getName());
				}
				break;
			}
		}

		return canInstantiate;
	}

	public static boolean hasFirstParameter(Method method, Class<?> type) {
		return method.getParameterCount() > 0 && type.isAssignableFrom(method.getParameterTypes()[0]);
	}

	@Nonnull
	public static Exception getException(Exception e) {
		while (e.getCause() != null) {
			e = (Exception) e.getCause();
		}

		return e;
	}

	public static Throwable getException(Throwable e) {
		while (e.getCause() != null) {
			e = e.getCause();
		}

		return e;
	}

	public static String randomId(int n) {
		final ThreadLocalRandom random = ThreadLocalRandom.current();

		final StringBuilder sb = new StringBuilder(64);
		for (int i = 0; i < n; i++) {
			sb.append(chars[random.nextInt(0, chars.length)]);
		}

		return sb.toString();
	}

	@Nonnull
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
}