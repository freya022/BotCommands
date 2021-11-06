package com.freya02.botcommands.internal.events;

import net.dv8tion.jda.api.events.Event;

@FunctionalInterface
public interface EventConsumer {
	void accept(Event event) throws Exception;
}
