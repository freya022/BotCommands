package com.freya02.botcommands.internal.components;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.internal.ExecutableInteractionInfo;
import com.freya02.botcommands.internal.MethodParameters;
import com.freya02.botcommands.internal.runner.MethodRunner;
import kotlin.reflect.KFunction;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;

public class ComponentDescriptor implements ExecutableInteractionInfo {
	private final Method method;
	private final Object instance;
	private final MethodRunner methodRunner;
	private final MethodParameters<ComponentHandlerParameter> componentParameters;

	public ComponentDescriptor(BContext context, Object instance, Method method) {
		this.method = method;
		this.instance = instance;
		this.methodRunner = context.getMethodRunnerFactory().make(instance, method);

		this.componentParameters = MethodParameters.of(method, ComponentHandlerParameter::new);
	}

	@Override
	public KFunction<?> getMethod() {
		return method;
	}

	@Override
	@NotNull
	public MethodRunner getMethodRunner() {
		return methodRunner;
	}

	@Override
	@NotNull
	public MethodParameters<ComponentHandlerParameter> getParameters() {
		return componentParameters;
	}

	@Override
	@NotNull
	public Object getInstance() {
		return instance;
	}
}
