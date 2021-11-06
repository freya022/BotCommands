package com.freya02.botcommands.internal.events;

import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class EventListenerImpl implements EventListener {
	private final Map<Class<?>, List<EventConsumer>> eventListeners;

	public EventListenerImpl(Map<Class<?>, List<EventConsumer>> eventListeners) {
		this.eventListeners = eventListeners;
	}

	@Override
	public void onEvent(@NotNull GenericEvent event) {
		if (!(event instanceof final Event realEvent)) return;

		final List<EventConsumer> eventConsumers = eventListeners.get(event.getClass());
		if (eventConsumers != null) {
			for (EventConsumer consumer : eventConsumers) {
				try {
					consumer.accept(realEvent);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}
