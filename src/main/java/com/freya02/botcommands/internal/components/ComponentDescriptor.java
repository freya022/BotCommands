package com.freya02.botcommands.internal.components;

import com.freya02.botcommands.api.parameters.ComponentParameterResolver;

import java.lang.reflect.Method;
import java.util.List;

public class ComponentDescriptor {
	private final Method method;
	private final List<ComponentParameterResolver> resolvers;
	private final Object instance;

	public ComponentDescriptor(Object instance, Method method, List<ComponentParameterResolver> resolvers) {
		this.method = method;
		this.resolvers = resolvers;
		this.instance = instance;
	}

	public Method getMethod() {
		return method;
	}

	public List<ComponentParameterResolver> getResolvers() {
		return resolvers;
	}

	public Object getInstance() {
		return instance;
	}
}
