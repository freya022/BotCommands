package com.freya02.botcommands.application;

import com.freya02.botcommands.internal.utils.Utils;
import com.freya02.botcommands.parameters.CustomResolver;
import com.freya02.botcommands.parameters.ParameterResolver;
import com.freya02.botcommands.parameters.ParameterResolvers;
import net.dv8tion.jda.api.events.Event;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Function;

public abstract class ApplicationCommandParameter<T> {
	private final T resolver;
	private final Class<?> type;
	private final CustomResolver customResolver;

	@SuppressWarnings("unchecked")
	public ApplicationCommandParameter(Class<T> resolverType, Class<?> type) {
		this.type = Utils.getBoxedType(type);

		final ParameterResolver resolver = ParameterResolvers.of(this.type);
		if (resolver == null) {
			throw new IllegalArgumentException("Unknown application command option type: " + type.getName() + " for target resolver " + resolverType.getName());
		} else if (resolver instanceof CustomResolver) {
			this.customResolver = (CustomResolver) resolver;
		} else if (!(resolverType.isAssignableFrom(resolver.getClass()))) {
			throw new IllegalArgumentException("Unsupported application command option type: " + type.getName() + " for target resolver " + resolverType.getName());
		} else {
			this.customResolver = null;
		}

		this.resolver = (T) resolver;
	}

	@Nonnull
	public Class<?> getType() {
		return type;
	}

	@Nullable
	public T getResolver() {
		return resolver;
	}

	@Nullable
	public CustomResolver getCustomResolver() {
		return customResolver;
	}
	
	@Nullable
	public <E extends Event> Object tryResolve(E event, Function<T, Object> function) {
		if (resolver != null) {
			return function.apply(getResolver());
		} else {
			final CustomResolver customResolver = getCustomResolver();
			if (customResolver == null)
				throw new IllegalStateException("Both resolvers are null for type " + getType().getName());

			return customResolver.resolve(event);
		}
	}
}
