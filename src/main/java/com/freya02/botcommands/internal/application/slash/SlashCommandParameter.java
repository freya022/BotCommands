package com.freya02.botcommands.internal.application.slash;

import com.freya02.botcommands.api.parameters.SlashParameterResolver;
import com.freya02.botcommands.internal.application.ApplicationCommandParameter;

import java.lang.reflect.Parameter;

public class SlashCommandParameter extends ApplicationCommandParameter<SlashParameterResolver> {
	public SlashCommandParameter(Parameter parameter, int index) {
		super(SlashParameterResolver.class, parameter, index);
	}
}
