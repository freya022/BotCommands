package com.freya02.botcommands.internal.prefixed;

import com.freya02.botcommands.api.parameters.RegexParameterResolver;
import com.freya02.botcommands.internal.application.CommandParameter;

import java.lang.reflect.Parameter;

public class TextCommandParameter extends CommandParameter<RegexParameterResolver> {
	private final int groupCount;

	public TextCommandParameter(Class<RegexParameterResolver> resolverType, Parameter parameter, int index) {
		super(resolverType, parameter, index);

		final RegexParameterResolver resolver = getResolver();

		if (resolver != null) {
			this.groupCount = resolver.getPreferredPattern().matcher("").groupCount();
		} else this.groupCount = -1;
	}

	public int getGroupCount() {
		return groupCount;
	}
}
