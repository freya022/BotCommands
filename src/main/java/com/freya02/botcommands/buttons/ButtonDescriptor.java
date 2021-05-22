package com.freya02.botcommands.buttons;

import com.freya02.botcommands.parameters.ParameterResolver;

import java.lang.reflect.Method;
import java.util.List;

public class ButtonDescriptor {
	private final Method method;
	private final List<ParameterResolver> resolvers;
	private final Object instance;

	public ButtonDescriptor(Object instance, Method method, List<ParameterResolver> resolvers) {
		this.method = method;
		this.resolvers = resolvers;
		this.instance = instance;
	}

	public Method getMethod() {
		return method;
	}

	public List<ParameterResolver> getResolvers() {
		return resolvers;
	}

	public Object getInstance() {
		return instance;
	}
}
