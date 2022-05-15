package com.freya02.botcommands.internal.components;

import com.freya02.botcommands.annotations.api.application.annotations.AppOption;
import com.freya02.botcommands.api.parameters.ComponentParameterResolver;
import com.freya02.botcommands.internal.application.CommandParameter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.List;

public class ComponentHandlerParameter extends CommandParameter<ComponentParameterResolver> {
	public ComponentHandlerParameter(Parameter parameter, int index) {
		super(ComponentParameterResolver.class, parameter, index);
	}

	@Override
	protected List<Class<? extends Annotation>> getOptionAnnotations() {
		return List.of(AppOption.class);
	}
}
