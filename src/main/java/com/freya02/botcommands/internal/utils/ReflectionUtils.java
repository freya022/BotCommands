package com.freya02.botcommands.internal.utils;

import com.freya02.botcommands.api.Logging;
import com.freya02.botcommands.api.annotations.ConditionalUse;
import com.freya02.botcommands.api.annotations.Optional;
import com.freya02.botcommands.api.application.slash.annotations.DoubleRange;
import com.freya02.botcommands.api.application.slash.annotations.LongRange;
import io.github.classgraph.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class ReflectionUtils {
	private static final StackWalker walker = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
	private static final Logger LOGGER = Logging.getLogger();

	private static final Map<Parameter, Map<Class<?>, Annotation>> paramAnnotationsMap = new HashMap<>();

	@NotNull
	public static Set<Class<?>> getPackageClasses(@NotNull String packageName, int maxDepth) throws IOException {
		final Set<Class<?>> classes = new HashSet<>();
		final String packagePath = packageName.replace('.', File.separatorChar);

		final String classPath = System.getProperty("java.class.path");
		for (String strPath : classPath.split(File.pathSeparator)) {
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
						final String relativePath = walkRoot.relativize(p)
								.toString()
								.replace(walkRoot.getFileSystem().getSeparator(),  ".");

						//Remove .class suffix and add package prefix
						final String result = packageName + "." + relativePath.substring(0, relativePath.length() - 6);

						try {
							classes.add(Class.forName(result, false, Utils.class.getClassLoader()));
						} catch (ClassNotFoundException e) {
							LOGGER.error("Unable to load class '{}' in class path '{}', isJAR = {}, filesystem: {}", result, strPath, isJar, walkRoot.getFileSystem(), e);
						}
					});
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
		final Map<Class<?>, Annotation> map = paramAnnotationsMap.get(parameter);
		if (map == null) return false;

		return map.containsKey(Optional.class) || map.containsKey(Nullable.class);
	}

	@Nullable
	public static LongRange getLongRange(Parameter parameter) {
		final Map<Class<?>, Annotation> map = paramAnnotationsMap.get(parameter);
		if (map == null) return null;

		return (LongRange) map.get(LongRange.class);
	}

	@Nullable
	public static DoubleRange getDoubleRange(Parameter parameter) {
		final Map<Class<?>, Annotation> map = paramAnnotationsMap.get(parameter);
		if (map == null) return null;

		return (DoubleRange) map.get(DoubleRange.class);
	}

	public static void scanAnnotations(Set<Class<?>> classes) {
		if (classes.isEmpty())
			return;

		final ScanResult result = new ClassGraph()
				.acceptClasses(classes.stream().map(Class::getName).toArray(String[]::new))
				.enableMethodInfo()
				.enableAnnotationInfo()
				.scan();

		for (ClassInfo classInfo : result.getAllClasses()) {
			for (MethodInfo methodInfo : classInfo.getDeclaredMethodInfo()) {
				final Parameter[] parameters = methodInfo.loadClassAndGetMethod().getParameters();

				int j = 0;
				for (MethodParameterInfo parameterInfo : methodInfo.getParameterInfo()) {
					final Parameter parameter = parameters[j++];

					for (AnnotationInfo annotationInfo : parameterInfo.getAnnotationInfo()) {
						paramAnnotationsMap.computeIfAbsent(parameter, x -> new HashMap<>()).put(annotationInfo.getClassInfo().loadClass(), annotationInfo.loadClassAndInstantiate());
					}
				}
			}
		}
	}

	@NotNull
	public static String formatFrameMethod(@NotNull StackWalker.StackFrame frame) {
		return "[Line %d] %s#%s(%s)".formatted(frame.getLineNumber(),
				frame.getDeclaringClass().getSimpleName(),
				frame.getMethodName(),
				frame.getMethodType().parameterList()
						.stream()
						.map(Class::getSimpleName)
						.collect(Collectors.joining(", "))
		);
	}

	@NotNull
	public static String formatCallerMethod() {
		final StackWalker.StackFrame callerFrame = getFrame(3);
		if (callerFrame == null) throw new IllegalStateException("Found no method caller");

		return formatFrameMethod(callerFrame);
	}

	@NotNull
	public static StackWalker.StackFrame getCallerFrame() {
		final StackWalker.StackFrame callerFrame = getFrame(3);
		if (callerFrame == null) throw new IllegalStateException("Found no method caller");

		return callerFrame;
	}

	@Nullable
	public static StackWalker.StackFrame getFrame(@Range(from = 0, to = Integer.MAX_VALUE) int skip) {
		return walker.walk(stackFrameStream -> stackFrameStream.skip(skip).findFirst().orElse(null));
	}

	@Nullable
	public static Class<?> getCollectionReturnType(Class<?> returnClass, Type returnType) {
		if (returnType instanceof ParameterizedType type) {
			final Class<?> rawType = (Class<?>) type.getRawType();

			if (Collection.class.isAssignableFrom(rawType) && type.getOwnerType() == null) {
				return (Class<?>) type.getActualTypeArguments()[0];
			}
		}

		List<Class<?>> superclasses = new ArrayList<>();
		List<Type> supertypes = new ArrayList<>();

		if (returnClass.getGenericSuperclass() != null) {
			superclasses.add(returnClass.getSuperclass());
			supertypes.add(returnClass.getGenericSuperclass());
		}

		superclasses.addAll(Arrays.asList(returnClass.getInterfaces()));
		supertypes.addAll(Arrays.asList(returnClass.getGenericInterfaces()));

		for (int i = 0, supertypesSize = supertypes.size(); i < supertypesSize; i++) {
			final Class<?> listReturnType = getCollectionReturnType(superclasses.get(i), supertypes.get(i));

			if (listReturnType != null) {
				return listReturnType;
			}
		}

		return null;
	}

	@Nullable
	public static Class<?> getCollectionReturnType(@NotNull Method method) {
		Type returnType = method.getGenericReturnType();

		return getCollectionReturnType(method.getReturnType(), returnType);
	}

	@NotNull
	public static Class<?> getCollectionTypeOrBoxedSelfType(@NotNull Parameter parameter) {
		final Class<?> type = getCollectionReturnType(parameter.getType(), parameter.getParameterizedType());
		if (type == null) {
			return Utils.getBoxedType(parameter.getType());
		}

		return type;
	}
}
