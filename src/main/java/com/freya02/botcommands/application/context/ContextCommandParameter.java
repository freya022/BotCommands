package com.freya02.botcommands.application.context;

import com.freya02.botcommands.parameters.ParameterResolver;
import com.freya02.botcommands.parameters.ParameterResolvers;

public class ContextCommandParameter<T> {
	private final T resolver;
	private final Class<?> type;

	@SuppressWarnings("unchecked")
	public ContextCommandParameter(Class<T> resolverType, Class<?> type) {
		this.type = type; //no need to get boxed type, it's going to be Discord entities
		
		final ParameterResolver resolver = ParameterResolvers.of(this.type);
		if (resolver == null) {
			throw new IllegalArgumentException("Unknown context command option type: " + type.getName());
		} else if (!(resolverType.isAssignableFrom(resolver.getClass()))) {
			throw new IllegalArgumentException("Unsupported context command option type: " + type.getName());
		}

		this.resolver = (T) resolver;
	}

	public Class<?> getType() {
		return type;
	}

	public T getResolver() {
		return resolver;
	}
}
