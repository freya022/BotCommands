package com.freya02.botcommands.prefixed.regex;

import com.freya02.botcommands.parameters.ParameterResolver;

public class ArgumentFunction {
	public final String pattern;
	public final int groups;
	public final ParameterResolver resolver;

	ArgumentFunction(String pattern, int groups, Class<?> type) {
		this(pattern, groups, ParameterResolver.of(type));
	}

	ArgumentFunction(String pattern, int groups, ParameterResolver resolver) {
		this.pattern = pattern;
		this.groups = groups;
		this.resolver = resolver;
	}

	ArgumentFunction optimize(String newPattern) {
		return new ArgumentFunction(newPattern, groups, resolver);
	}
}
