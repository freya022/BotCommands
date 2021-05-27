package com.freya02.botcommands.slash.parameters;

import com.freya02.botcommands.parameters.ParameterResolver;

public class SlashCommandParameter {
	private final boolean optional;
	private final String effectiveName;
	private final ParameterResolver resolver;
	private final Class<?> type;

	public SlashCommandParameter(boolean optional, String effectiveName, Class<?> type) {
		this.optional = optional;
		this.effectiveName = effectiveName;

		this.resolver = ParameterResolver.of(type);
		this.type = type;
		if (resolver == null) {
			throw new IllegalArgumentException("Unknown slash command option type: " + type.getName());
		} else if (!resolver.isSlashCommandSupported()) {
			throw new IllegalArgumentException("Unsupported slash command option type: " + type.getName());
		}
	}

	public Class<?> getType() {
		return type;
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
