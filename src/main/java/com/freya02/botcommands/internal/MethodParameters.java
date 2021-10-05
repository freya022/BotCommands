package com.freya02.botcommands.internal;

import com.freya02.botcommands.internal.application.CommandParameter;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.function.BiFunction;

public class MethodParameters<T extends CommandParameter<?>> extends ArrayList<T> {
	private final int optionCount;

	private MethodParameters(Method method, BiFunction<Parameter, Integer, T> function) {
		super(method.getParameterCount());
		
		final Parameter[] parameters = method.getParameters();
		for (int i = 1, parametersLength = parameters.length; i < parametersLength; i++) {
			add(function.apply(parameters[i], i));
		}

		this.optionCount = (int) stream().filter(CommandParameter::isOption).count();
	}

	public int getOptionCount() {
		return optionCount;
	}

	public static <T extends CommandParameter<?>> MethodParameters<T> of(Method method, BiFunction<Parameter, Integer, T> function) {
		return new MethodParameters<>(method, function);
	}
}
