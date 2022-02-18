package com.freya02.botcommands.internal;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.internal.application.CommandParameter;
import com.freya02.botcommands.internal.runner.MethodRunnerFactory;
import com.freya02.botcommands.internal.utils.Utils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.BiFunction;

public class MethodParameters<T extends CommandParameter<?>> extends ArrayList<T> {
	private final int optionCount;

	private MethodParameters(BContext context, Method method, BiFunction<Parameter, Integer, T> function) {
		super(getParameterCount(context, method));

		final Parameter[] parameters = getParameters(context, method);

		//Check if the last argument is a kotlin Continuation (injected into suspend methods)
		// If we can see it in this parameter list, then this means the target method was not recognized as a suspend method
		// Thus the default MethodRunnerFactory will not support this method
		if (parameters[parameters.length - 1].getType().getName().equals("kotlin.coroutines.Continuation")) {
			throw new IllegalArgumentException(
					"""
							Detected a Kotlin suspending method, these methods are not supported by the current MethodRunnerFactory.
							Current MethodRunnerFactory: '%s'
							Suspending method at: %s
							Hint: You can use KotlinMethodRunnerFactory in ExtensionsBuilder#setMethodRunnerFactory""".formatted(
							context.getMethodRunnerFactory().getClass().getName(),
							Utils.formatMethodShort(method)
					)
			);
		}

		for (int i = 1, parametersLength = parameters.length; i < parametersLength; i++) {
			add(function.apply(parameters[i], i));
		}

		this.optionCount = (int) stream().filter(CommandParameter::isOption).count();
	}

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

	public static <T extends CommandParameter<?>> MethodParameters<T> of(BContext context, Method method, BiFunction<Parameter, Integer, T> function) {
		return new MethodParameters<>(context, method, function);
	}

	public int getOptionCount() {
		return optionCount;
	}
}
