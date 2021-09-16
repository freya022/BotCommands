package com.freya02.botcommands.internal.components;

import com.freya02.botcommands.api.parameters.ComponentParameterResolver;
import com.freya02.botcommands.internal.MethodParameters;
import com.freya02.botcommands.internal.application.InteractionParameter;

import java.lang.reflect.Method;

public class ComponentDescriptor {
	private final Method method;
	private final Object instance;
	private final MethodParameters<InteractionParameter<ComponentParameterResolver>> componentParameters;

	public ComponentDescriptor(Object instance, Method method) {
		this.method = method;
		this.instance = instance;

		this.componentParameters = MethodParameters.of(method, (parameter, index) -> {
			return new InteractionParameter<>(ComponentParameterResolver.class, parameter, index);
		});
	}

	public Method getMethod() {
		return method;
	}

	public MethodParameters<InteractionParameter<ComponentParameterResolver>> getParameters() {
		return componentParameters;
	}

	public Object getInstance() {
		return instance;
	}
}
