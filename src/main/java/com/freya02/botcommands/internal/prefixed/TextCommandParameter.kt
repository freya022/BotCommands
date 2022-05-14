package com.freya02.botcommands.internal.prefixed;

import com.freya02.botcommands.annotations.api.prefixed.annotations.TextOption;
import com.freya02.botcommands.api.parameters.RegexParameterResolver;
import com.freya02.botcommands.internal.application.CommandParameter;
import kotlin.reflect.KParameter;

import java.lang.annotation.Annotation;
import java.util.List;

public class TextCommandParameter extends CommandParameter<RegexParameterResolver> {
	private final int groupCount;
	private final TextParameterData data;
	private final boolean isId;

	public TextCommandParameter(Class<RegexParameterResolver> resolverType, KParameter parameter, int index) {
		super(resolverType, parameter, index);

		final RegexParameterResolver resolver = getResolver();

		if (resolver != null) {
			this.groupCount = resolver.getPreferredPattern().matcher("").groupCount();
		} else this.groupCount = -1;

		this.data = new TextParameterData(parameter);
		this.isId = true; //TODO fix
	}

	@Override
	protected List<Class<? extends Annotation>> getOptionAnnotations() {
		return List.of(TextOption.class);
	}

	public TextParameterData getData() {
		return data;
	}

	public int getGroupCount() {
		return groupCount;
	}

	public boolean isId() {
		return isId;
	}
}
