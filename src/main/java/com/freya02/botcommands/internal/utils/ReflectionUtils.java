package com.freya02.botcommands.internal.utils;

import com.freya02.botcommands.api.Logging;
import com.freya02.botcommands.api.annotations.ConditionalUse;
import com.freya02.botcommands.api.annotations.Optional;
import com.freya02.botcommands.api.application.CommandScope;
import com.freya02.botcommands.api.application.slash.annotations.DoubleRange;
import com.freya02.botcommands.api.application.slash.annotations.LongRange;
import io.github.classgraph.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import org.slf4j.Logger;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

public class ReflectionUtils {
	private static final StackWalker walker = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
	private static final Logger LOGGER = Logging.getLogger();

	private static final Map<Parameter, Map<Class<?>, Annotation>> paramAnnotationsMap = new HashMap<>();

	@NotNull
	public static Set<Class<?>> scanPackagesAndClasses(Set<String> packageNames, Set<Class<?>> manualClasses) {
		final boolean hasPackages = !packageNames.isEmpty();
		final boolean hasClasses = !manualClasses.isEmpty();
		if (!hasPackages && !hasClasses) {
			LOGGER.warn("No packages or classes were registered");
			return Collections.emptySet();
		}

		try (ScanResult scanResult = new ClassGraph()
				.acceptPackages(packageNames.toArray(String[]::new))
				.acceptClasses(manualClasses.stream().map(Class::getName).toArray(String[]::new))
				.enableMethodInfo()
				.enableAnnotationInfo()
				.scan()) {
			final ClassInfoList allStandardClasses = scanResult.getAllStandardClasses();
			if (allStandardClasses.isEmpty()) {
				if (hasPackages && !hasClasses) {
					LOGGER.warn("No classes have been found as nothing was found in packages and no classes were manually registered");
					LOGGER.warn("Packages: {}", String.join(", ", packageNames));
				} else if (hasPackages/* && hasClasses*/) {
					LOGGER.error("No classes have been found despite packages and classes being added, please report this to the developers");
					LOGGER.error("Packages: {}", String.join(", ", packageNames));
					LOGGER.error("Classes: {}", manualClasses.stream().map(Class::getSimpleName).collect(Collectors.joining(", ")));
				}
			}

			final var instantiableClasses = allStandardClasses.filter(ReflectionUtils::isInstantiable);

			scanAnnotations(instantiableClasses);

			return Set.copyOf(instantiableClasses.loadClasses());
		}
	}

	private static boolean isInstantiable(ClassInfo classInfo) {
		try {
			final MethodInfoList methodInfos = classInfo.getDeclaredMethodInfo().filter(m -> m.hasAnnotation(ConditionalUse.class));
			if (methodInfos.isEmpty())
				return true;
			if (methodInfos.size() > 1)
				throw new IllegalArgumentException("Class %s must have at most one method annotated with @%s".formatted(classInfo.getSimpleName(), ConditionalUse.class.getSimpleName()));

			final MethodInfo methodInfo = methodInfos.get(0);
			if (!methodInfo.isStatic())
				throw new IllegalArgumentException("@%s at %s#%s must be static".formatted(ConditionalUse.class.getSimpleName(), classInfo.getSimpleName(), methodInfo.getName()));
			if (methodInfo.getParameterInfo().length != 0)
				throw new IllegalArgumentException("@%s at %s#%s must have 0 parameters".formatted(ConditionalUse.class.getSimpleName(), classInfo.getSimpleName(), methodInfo.getName()));
			if (!(methodInfo.getTypeSignatureOrTypeDescriptor().getResultType() instanceof BaseTypeSignature baseTypeSignature) || baseTypeSignature.getType() != Boolean.TYPE)
				throw new IllegalArgumentException("@%s at %s#%s must return a boolean".formatted(ConditionalUse.class.getSimpleName(), classInfo.getSimpleName(), methodInfo.getName()));

			final Method method = methodInfo.loadClassAndGetMethod();
			if (!method.canAccess(null))
				throw new IllegalArgumentException("@%s at %s#%s must be public".formatted(ConditionalUse.class.getSimpleName(), classInfo.getSimpleName(), methodInfo.getName()));
			return (boolean) method.invoke(null);
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	private static void scanAnnotations(ClassInfoList instantiableClasses) {
		for (ClassInfo instantiableClass : instantiableClasses) {
			for (MethodInfo methodInfo : instantiableClass.getDeclaredMethodInfo()) {
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

	public static boolean hasFirstParameter(Method method, Class<?> type) {
		return method.getParameterCount() > 0 && type.isAssignableFrom(method.getParameterTypes()[0]);
	}

	public static void checkApplicationCommandParameter(Method method, CommandScope scope, Class<?> globalType, Class<?> guildType) {
		if (method.getParameterCount() == 0) {
			throw new IllegalArgumentException("Application command at " + Utils.formatMethodShort(method) + " must have a " + guildType.getSimpleName() + " or " + globalType.getSimpleName() + " as first parameter");
		}

		final Class<?> firstParamType = method.getParameterTypes()[0];
		if (scope.isGuildOnly()) {
			//Guild type or lower
			if (!firstParamType.isAssignableFrom(guildType))
				throw new IllegalArgumentException("Application command at " + Utils.formatMethodShort(method) + " must have a " + guildType.getSimpleName() + " or " + globalType.getSimpleName() + " as first parameter");
			if (!guildType.isAssignableFrom(firstParamType)) {
				//If type is correct but guild specialization isn't used
				LOGGER.warn("Guild-only application command {} uses {}, consider using {} to remove warnings related to guild stuff's nullability", Utils.formatMethodShort(method), firstParamType.getSimpleName(), guildType.getSimpleName());
			}
		} else {
			//Global scope, need a global type or lower
			if (!firstParamType.isAssignableFrom(globalType))
				throw new IllegalArgumentException("Application command at " + Utils.formatMethodShort(method) + " must have a " + globalType.getSimpleName() +" as first parameter");
		}
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
