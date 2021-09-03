package com.freya02.botcommands.parameters;

import com.freya02.botcommands.application.slash.annotations.Option;
import net.dv8tion.jda.api.events.Event;

import javax.annotation.Nullable;
import java.util.function.Function;

/**
 * Represents a custom resolver for interaction parameters
 * <br>This does not need to be implemented unlike other resolvers such as {@link SlashParameterResolver}
 * <br><b>This resolver only gets used if an interaction parameter is not annotated with {@link Option}</b>
 */
public final class CustomResolver extends ParameterResolver {
	private final Function<Event, ?> function;

	public <T> CustomResolver(Class<T> clazz, Function<Event, T> function) {
		super(clazz);
		
		this.function = function;
	}

	@Nullable
	public Object resolve(Event event) {
		return function.apply(event);
	}
}
