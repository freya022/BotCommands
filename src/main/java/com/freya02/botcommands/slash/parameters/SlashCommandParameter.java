package com.freya02.botcommands.slash.parameters;

import com.freya02.botcommands.parameters.ParameterResolver;

public class SlashCommandParameter {
	private final boolean optional;
	private final String effectiveName;
	private final ParameterResolver resolver;

	public SlashCommandParameter(boolean optional, String effectiveName, ParameterResolver resolver) {
		this.optional = optional;
		this.effectiveName = effectiveName;
		this.resolver = resolver;
	}

	public boolean isOptional() {
		return optional;
	}

	public String getEffectiveName() {
		return effectiveName;
	}

	public ParameterResolver getResolver() {
		return resolver;
	}
}
