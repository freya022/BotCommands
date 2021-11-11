package com.freya02.botcommands.internal.utils;

import com.freya02.botcommands.api.annotations.ConditionalUse;
import com.freya02.botcommands.internal.Logging;
import io.github.classgraph.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class ReflectionUtils {
	private static final Logger LOGGER = Logging.getLogger();

	private static final Set<Parameter> optionalSet = new HashSet<>();

	@NotNull
	public static Set<Class<?>> getPackageClasses(@NotNull String packageName, int maxDepth) throws IOException {
		final Set<Class<?>> classes = new HashSet<>();
		final String packagePath = packageName.replace('.', File.separatorChar);

		final String classPath = System.getProperty("java.class.path");
		for (String strPath : classPath.split(";")) {
			final Path jarPath = Path.of(strPath);

			final Path walkRoot;
			final boolean isJar = strPath.endsWith("jar");
			if (isJar) {
				final FileSystem zfs = FileSystems.newFileSystem(jarPath, (ClassLoader) null);
				walkRoot = zfs.getPath(packagePath);
			} else {
				walkRoot = jarPath.resolve(packagePath);
			}

			if (Files.notExists(walkRoot)) {
				continue;
			}

			Files.walk(walkRoot, maxDepth)
					.filter(Files::isRegularFile)
					.filter(p -> IOUtils.getFileExtension(p).equals("class"))
					.forEach(p -> {
						// Change from a/b/c/d to c/d
						final String relativePath = walkRoot.relativize(p).toString().replace('\\',  '.');

						//Remove .class suffix and add package prefix
						final String result = packageName + "." + relativePath.substring(0, relativePath.length() - 6);

						try {
							classes.add(Class.forName(result, false, Utils.class.getClassLoader()));
						} catch (ClassNotFoundException e) {
							LOGGER.error("Unable to load class {}", result);
						}
					});

			break;
		}

		return classes;
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

	public static boolean isOptional(Parameter parameter) {
		return optionalSet.contains(parameter);
	}

	public static void scanOptionals(Set<Class<?>> classes) {
		if (classes.isEmpty())
			return;

		final ScanResult result = new ClassGraph()
				.acceptClasses(classes.stream().map(Class::getName).toArray(String[]::new))
				.enableMethodInfo()
				.enableAnnotationInfo()
				.scan();

		final ClassInfoList nullableInfos = result.getClassesWithMethodParameterAnnotation("org.jetbrains.annotations.Nullable")
				.union(result.getClassesWithMethodParameterAnnotation("javax.annotation.Nullable"))
				.union(result.getClassesWithMethodParameterAnnotation("com.freya02.botcommands.api.annotations.Optional"));

		for (ClassInfo classInfo : nullableInfos) {
			for (MethodInfo info : classInfo.getDeclaredMethodInfo()) {
				if (info.isConstructor())
					continue;

				final Method method = info.loadClassAndGetMethod();

				MethodParameterInfo[] infoParameterInfo = info.getParameterInfo();
				for (int i = 0, infoParameterInfoLength = infoParameterInfo.length; i < infoParameterInfoLength; i++) {
					MethodParameterInfo parameterInfo = infoParameterInfo[i];

					if (!parameterInfo.hasAnnotation("org.jetbrains.annotations.Nullable")
							&& !parameterInfo.hasAnnotation("com.freya02.botcommands.api.annotations.Optional")
							&& !parameterInfo.hasAnnotation("javax.annotation.Nullable")) continue;

					optionalSet.add(method.getParameters()[i]);
				}
			}
		}
	}
}
