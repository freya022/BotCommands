package com.freya02.botcommands.internal.application.slash.autocomplete;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class ClassUtils {
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

	@Nullable
	public static Class<?> getCollectionReturnType(@NotNull Parameter parameter) {
		return getCollectionReturnType(parameter.getType(), parameter.getParameterizedType());
	}
}
