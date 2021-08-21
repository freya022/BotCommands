package com.freya02.botcommands.internal.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;

public class AnnotationUtils {
	@SuppressWarnings("unchecked")
	public static <T, A extends Annotation> T getAnnotationValue(A annotation, String methodName) {
		try {
			return (T) annotation.annotationType().getMethod(methodName).invoke(annotation);
		} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			throw new RuntimeException("Unable to get annotation value from " + annotation.annotationType().getName() + "#" + methodName, e);
		}
	}
}
