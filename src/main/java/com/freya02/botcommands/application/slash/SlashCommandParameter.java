package com.freya02.botcommands.application.slash;

import com.freya02.botcommands.Utils;
import com.freya02.botcommands.parameters.ParameterResolver;
import com.freya02.botcommands.parameters.ParameterResolvers;
import com.freya02.botcommands.parameters.SlashParameterResolver;

public class SlashCommandParameter {
	private final boolean optional;
	private final String effectiveName;
	private final SlashParameterResolver resolver;
	private final Class<?> type;

	public SlashCommandParameter(boolean optional, String effectiveName, Class<?> type) {
		this.optional = optional;
		this.effectiveName = effectiveName;

		this.type = Utils.getBoxedType(type);
		ParameterResolver resolver = ParameterResolvers.of(this.type);
		if (resolver == null) {
			throw new IllegalArgumentException("Unknown slash command option type: " + type.getName());
		} else if (!(resolver instanceof SlashParameterResolver)) {
			throw new IllegalArgumentException("Unsupported slash command option type: " + type.getName());
		}

		this.resolver = (SlashParameterResolver) resolver;
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

	public SlashParameterResolver getResolver() {
		return resolver;
	}
}
