package com.freya02.botcommands.application.slash;

import com.freya02.botcommands.application.ApplicationCommandParameter;
import com.freya02.botcommands.parameters.SlashParameterResolver;

import java.lang.reflect.Parameter;

public class SlashCommandParameter extends ApplicationCommandParameter<SlashParameterResolver> {
	public SlashCommandParameter(Parameter parameter, int index) {
		super(SlashParameterResolver.class, parameter, index);
	}
}
