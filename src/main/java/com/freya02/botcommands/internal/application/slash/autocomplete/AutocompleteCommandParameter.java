package com.freya02.botcommands.internal.application.slash.autocomplete;

import com.freya02.botcommands.api.application.slash.autocomplete.annotations.CompositeKey;
import com.freya02.botcommands.api.parameters.SlashParameterResolver;
import com.freya02.botcommands.internal.application.ApplicationCommandParameter;

import java.lang.reflect.Parameter;

public class AutocompleteCommandParameter extends ApplicationCommandParameter<SlashParameterResolver> {
	private final boolean compositeKey;

	public AutocompleteCommandParameter(Parameter parameter, int index) {
		super(SlashParameterResolver.class, parameter, index);

		this.compositeKey = parameter.isAnnotationPresent(CompositeKey.class);
	}

	public boolean isCompositeKey() {
		return compositeKey;
	}
}
