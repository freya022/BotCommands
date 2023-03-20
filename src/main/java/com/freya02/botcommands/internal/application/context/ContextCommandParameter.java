package com.freya02.botcommands.internal.application.context;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.application.CommandPath;
import com.freya02.botcommands.internal.application.ApplicationCommandParameter;

import java.lang.reflect.Parameter;

public class ContextCommandParameter<T> extends ApplicationCommandParameter<T> {
	public ContextCommandParameter(BContext context, CommandPath path, Class<T> resolverType, Parameter parameter, int index) {
		super(context, path, resolverType, parameter, index);
	}
}