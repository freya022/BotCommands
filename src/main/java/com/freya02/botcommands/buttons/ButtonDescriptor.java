package com.freya02.botcommands.buttons;

import com.freya02.botcommands.parameters.ButtonParameterResolver;

import java.lang.reflect.Method;
import java.util.List;

public class ButtonDescriptor {
	private final Method method;
	private final List<ButtonParameterResolver> resolvers;
	private final Object instance;

	public ButtonDescriptor(Object instance, Method method, List<ButtonParameterResolver> resolvers) {
		this.method = method;
		this.resolvers = resolvers;
		this.instance = instance;
	}

	public Method getMethod() {
		return method;
	}

	public List<ButtonParameterResolver> getResolvers() {
		return resolvers;
	}

	public Object getInstance() {
		return instance;
	}
}
