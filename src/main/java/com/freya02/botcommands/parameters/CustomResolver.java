package com.freya02.botcommands.parameters;

import net.dv8tion.jda.api.events.Event;

import javax.annotation.Nullable;
import java.util.function.Function;

public class CustomResolver extends ParameterResolver {
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
