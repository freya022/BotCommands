package com.freya02.botcommands.internal;

import com.freya02.botcommands.internal.application.ApplicationCommandParameter;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.function.BiFunction;

public class MethodParameters<T extends ApplicationCommandParameter<?>> extends ArrayList<T> {
	private MethodParameters(Method method, BiFunction<Parameter, Integer, T> function) {
		super(method.getParameterCount());
		
		final Parameter[] parameters = method.getParameters();
		for (int i = 1, parametersLength = parameters.length; i < parametersLength; i++) {
			add(function.apply(parameters[i], i));
		}
	}
	
	public static <T extends ApplicationCommandParameter<?>> MethodParameters<T> of(Method method, BiFunction<Parameter, Integer, T> function) {
		return new MethodParameters<>(method, function);
	}
}
