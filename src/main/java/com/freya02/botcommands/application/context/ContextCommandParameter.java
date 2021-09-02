package com.freya02.botcommands.application.context;

import com.freya02.botcommands.application.ApplicationCommandParameter;

public class ContextCommandParameter<T> extends ApplicationCommandParameter<T> {
	public ContextCommandParameter(Class<T> resolverType, Class<?> type) {
		super(resolverType, type);
	}
}