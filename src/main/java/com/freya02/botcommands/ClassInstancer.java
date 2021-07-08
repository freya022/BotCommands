package com.freya02.botcommands;

import com.freya02.botcommands.annotation.Dependency;
import com.freya02.botcommands.prefixed.Command;

import javax.annotation.Nullable;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class ClassInstancer {
	@Nullable
	public static Object getMethodTarget(BContextImpl context, Method method) throws InvocationTargetException, InstantiationException, IllegalAccessException {
		if (Modifier.isStatic(method.getModifiers())) {
			return null;
		}

		return instantiate(context, method.getDeclaringClass());
	}

	public static Object instantiate(BContextImpl context, Class<?> aClass) throws InvocationTargetException, InstantiationException, IllegalAccessException {
		final Object oldInstance = context.getClassInstance(aClass);
		if (oldInstance != null)
			return oldInstance;

		final Object instance;

		//The command object has to be created either by the instance supplier
		// or by the **only** constructor a class has
		// It must resolve all parameters types with the registered parameter suppliers
		final InstanceSupplier<?> instanceSupplier = context.getInstanceSupplier(aClass);
		if (instanceSupplier != null) {
			instance = instanceSupplier.get(context);

			if (instance == null) {
				throw new IllegalArgumentException("InstanceSupplier from " + instanceSupplier.getClass() + " of " + aClass + " returned null");
			}
		} else {
			final Constructor<?>[] constructors = aClass.getConstructors();
			if (constructors.length == 0)
				throw new IllegalArgumentException("Class " + aClass.getName() + " must have an accessible constructor");

			if (constructors.length > 1)
				throw new IllegalArgumentException("Class " + aClass.getName() + " must have exactly one constructor");

			final Constructor<?> constructor = constructors[0];

			if (Command.class.isAssignableFrom(aClass)) { //Obligatory arg
				if (Arrays.stream(constructor.getParameterTypes()).noneMatch(BContext.class::isAssignableFrom))
					throw new IllegalArgumentException("Class " + aClass.getName() + " should have a BContext in its constructor arguments");
			}

			List<Object> parameterObjs = new ArrayList<>();

			Class<?>[] parameterTypes = constructor.getParameterTypes();
			for (int i = 0, parameterTypesLength = parameterTypes.length; i < parameterTypesLength; i++) {
				Class<?> parameterType = parameterTypes[i];

				if (BContext.class.isAssignableFrom(parameterType)) {
					parameterObjs.add(context);
				} else {
					final ConstructorParameterSupplier<?> supplier = context.getParameterSupplier(parameterType);
					if (supplier == null)
						throw new IllegalArgumentException(String.format("Found no constructor parameter supplier for parameter #%d of type %s in class %s", i, parameterType.getSimpleName(), aClass.getSimpleName()));

					parameterObjs.add(supplier.get(aClass));
				}
			}

			instance = constructor.newInstance(parameterObjs.toArray());
		}

		injectDependencies(context, instance);

		context.putClassInstance(aClass, instance);

		return instance;
	}

	private static void injectDependencies(BContextImpl context, Object someCommand) throws IllegalAccessException {
		for (Field field : someCommand.getClass().getDeclaredFields()) {
			if (!field.isAnnotationPresent(Dependency.class)) continue;

			if (!field.canAccess(someCommand)) {
				if (!field.trySetAccessible()) {
					throw new IllegalArgumentException("Dependency field " + field + " is not accessible (make it public ?)");
				}
			}

			final Supplier<?> dependencySupplier = context.getCommandDependency(field.getType());
			if (dependencySupplier == null) {
				throw new IllegalArgumentException("Dependency supplier for field " + field + " was not set");
			}

			field.set(someCommand, dependencySupplier.get());
		}
	}
}
