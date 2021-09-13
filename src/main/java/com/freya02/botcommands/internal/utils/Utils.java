package com.freya02.botcommands.internal.utils;

import com.freya02.botcommands.BContext;
import com.freya02.botcommands.annotation.ConditionalUse;
import com.freya02.botcommands.components.ComponentManager;
import com.freya02.botcommands.internal.Logging;
import io.github.classgraph.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public final class Utils {
	@SuppressWarnings("SpellCheckingInspection")
	private static final char[] chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ!\"#$%&'()*+,-./:;<=>?_^[]{}|".toCharArray();

	private static final Logger LOGGER = Logging.getLogger();
	private static final Set<Parameter> optionalSet = new HashSet<>();

	@SuppressWarnings("RedundantCast")
	public static List<Class<?>> getClasses(Path jarPath, String packageName, int maxDepth) throws IOException {
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

	public static boolean isInstantiable(Class<?> aClass) throws IllegalAccessException, InvocationTargetException {
		boolean canInstantiate = true;
		for (Method declaredMethod : aClass.getDeclaredMethods()) {
			if (declaredMethod.isAnnotationPresent(ConditionalUse.class)) {
				if (Modifier.isStatic(declaredMethod.getModifiers())) {
					if (declaredMethod.getParameterCount() == 0 && declaredMethod.getReturnType() == boolean.class) {
						if (!declaredMethod.canAccess(null))
							throw new IllegalStateException("Method " + Utils.formatMethodShort(declaredMethod) + " is not public");
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

	public static String formatMethodShort(Method method) {
		return method.getDeclaringClass().getSimpleName()
				+ "#"
				+ method.getName()
				+ Arrays.stream(method.getParameterTypes())
				.map(Class::getSimpleName)
				.collect(Collectors.joining(", ", "(", ")"));
	}

	public static boolean isOptional(Parameter parameter) {
		return optionalSet.contains(parameter);
	}

	public static void scanOptionals(Set<Class<?>> classes) {
		final ScanResult result = new ClassGraph()
				.acceptClasses(classes.stream().map(Class::getName).toArray(String[]::new))
				.enableMethodInfo()
				.enableAnnotationInfo()
				.scan();

		final ClassInfoList nullableInfos = result.getClassesWithMethodParameterAnnotation("org.jetbrains.annotations.Nullable")
				.union(result.getClassesWithMethodParameterAnnotation("javax.annotation.Nullable"))
				.union(result.getClassesWithMethodParameterAnnotation("com.freya02.botcommands.annotation.Optional"));

		for (ClassInfo classInfo : nullableInfos) {
			for (MethodInfo info : classInfo.getDeclaredMethodInfo()) {
				final Method method = info.loadClassAndGetMethod();

				MethodParameterInfo[] infoParameterInfo = info.getParameterInfo();
				for (int i = 0, infoParameterInfoLength = infoParameterInfo.length; i < infoParameterInfoLength; i++) {
					MethodParameterInfo parameterInfo = infoParameterInfo[i];

					if (!parameterInfo.hasAnnotation("org.jetbrains.annotations.Nullable")
							&& !parameterInfo.hasAnnotation("com.freya02.botcommands.annotation.Optional")
							&& !parameterInfo.hasAnnotation("javax.annotation.Nullable")) continue;

					optionalSet.add(method.getParameters()[i]);
				}
			}
		}
	}
}