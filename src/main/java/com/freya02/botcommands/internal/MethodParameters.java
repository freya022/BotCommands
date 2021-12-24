package com.freya02.botcommands.internal;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.internal.application.CommandParameter;
import com.freya02.botcommands.internal.runner.MethodRunnerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.BiFunction;

public class MethodParameters<T extends CommandParameter<?>> extends ArrayList<T> {
	private final int optionCount;

	private static int getParameterCount(BContext context, Method method) {
		final MethodRunnerFactory runnerFactory = context.getMethodRunnerFactory();

		if (runnerFactory.supportsSuspend() && runnerFactory.isSuspend(method)) {
			return method.getParameterCount() - 1;
		}

		return method.getParameterCount();
	}

	private static Parameter[] getParameters(BContext context, Method method) {
		final MethodRunnerFactory runnerFactory = context.getMethodRunnerFactory();

		if (runnerFactory.supportsSuspend() && runnerFactory.isSuspend(method)) {
			return Arrays.copyOf(method.getParameters(), method.getParameterCount() - 1);
		}

		return method.getParameters();
	}

	private MethodParameters(BContext context, Method method, BiFunction<Parameter, Integer, T> function) {
		super(getParameterCount(context, method));
		
		final Parameter[] parameters = getParameters(context, method);
		for (int i = 1, parametersLength = parameters.length; i < parametersLength; i++) {
			add(function.apply(parameters[i], i));
		}

		this.optionCount = (int) stream().filter(CommandParameter::isOption).count();
	}

	public int getOptionCount() {
		return optionCount;
	}

	public static <T extends CommandParameter<?>> MethodParameters<T> of(BContext context, Method method, BiFunction<Parameter, Integer, T> function) {
		return new MethodParameters<>(context, method, function);
	}
}
