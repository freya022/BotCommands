package com.freya02.botcommands.internal.application.context;

import com.freya02.botcommands.internal.application.ApplicationCommandParameter;

import java.lang.reflect.Parameter;

public class ContextCommandParameter<T> extends ApplicationCommandParameter<T> {
	public ContextCommandParameter(Class<T> resolverType, Parameter parameter, int index) {
		super(resolverType, parameter, index);
	}
}