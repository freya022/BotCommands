package com.freya02.botcommands.application.slash;

import com.freya02.botcommands.application.ApplicationCommandParameter;
import com.freya02.botcommands.parameters.SlashParameterResolver;

import javax.annotation.Nonnull;

public class SlashCommandParameter extends ApplicationCommandParameter<SlashParameterResolver> {
	private final boolean optional, primitive;
	private final String effectiveName;

	public SlashCommandParameter(boolean optional, String effectiveName, Class<?> type) {
		super(SlashParameterResolver.class, type);
		
		this.optional = optional;
		this.effectiveName = effectiveName;

		this.primitive = type.isPrimitive();
	}

	public boolean isPrimitive() {
		return primitive;
	}

	public boolean isOptional() {
		return optional;
	}

	@Nonnull
	public String getEffectiveName() {
		return effectiveName;
	}
}
