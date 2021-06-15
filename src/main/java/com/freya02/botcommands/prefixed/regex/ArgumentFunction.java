package com.freya02.botcommands.prefixed.regex;

import com.freya02.botcommands.parameters.ParameterResolver;
import com.freya02.botcommands.parameters.ParameterResolvers;
import com.freya02.botcommands.parameters.RegexParameterResolver;

public class ArgumentFunction {
	public final String pattern;
	public final int groups;
	public final RegexParameterResolver resolver;

	ArgumentFunction(String pattern, int groups, Class<?> type) {
		this.pattern = pattern;
		this.groups = groups;

		final ParameterResolver resolver = ParameterResolvers.of(type);

		if (!(resolver instanceof RegexParameterResolver)) {
			throw new IllegalArgumentException("Resolver is not supported");
		}

		this.resolver = (RegexParameterResolver) resolver;
	}

	ArgumentFunction(String pattern, int groups, RegexParameterResolver resolver) {
		this.pattern = pattern;
		this.groups = groups;
		this.resolver = resolver;
	}

	ArgumentFunction optimize(String newPattern) {
		return new ArgumentFunction(newPattern, groups, resolver);
	}
}
