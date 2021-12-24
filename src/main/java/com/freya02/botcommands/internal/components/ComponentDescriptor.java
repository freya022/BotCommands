package com.freya02.botcommands.internal.components;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.parameters.ComponentParameterResolver;
import com.freya02.botcommands.internal.MethodParameters;
import com.freya02.botcommands.internal.application.CommandParameter;
import com.freya02.botcommands.internal.runner.MethodRunner;

import java.lang.reflect.Method;

public class ComponentDescriptor {
	private final Method method;
	private final Object instance;
	private final MethodRunner methodRunner;
	private final MethodParameters<CommandParameter<ComponentParameterResolver>> componentParameters;

	public ComponentDescriptor(BContext context, Object instance, Method method) {
		this.method = method;
		this.instance = instance;
		this.methodRunner = context.getMethodRunnerFactory().make(instance, method);

		this.componentParameters = MethodParameters.of(context, method, (parameter, index) -> {
			return new CommandParameter<>(ComponentParameterResolver.class, parameter, index);
		});
	}

	public Method getMethod() {
		return method;
	}

	public MethodRunner getMethodRunner() {
		return methodRunner;
	}

	public MethodParameters<CommandParameter<ComponentParameterResolver>> getParameters() {
		return componentParameters;
	}

	public Object getInstance() {
		return instance;
	}
}
