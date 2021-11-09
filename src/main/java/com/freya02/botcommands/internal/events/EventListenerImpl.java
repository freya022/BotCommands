package com.freya02.botcommands.internal.events;

import com.freya02.botcommands.api.ExceptionHandler;
import com.freya02.botcommands.internal.BContextImpl;
import com.freya02.botcommands.internal.utils.Utils;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

class EventListenerImpl implements EventListener {
	private final BContextImpl context;
	private final Map<Class<?>, List<EventConsumer>> eventListeners;

	private int eventThreadNumber;
	private final ExecutorService eventExecutor = Utils.createCommandPool(r -> {
		final Thread thread = new Thread(r);
		thread.setDaemon(false);
		thread.setUncaughtExceptionHandler((t, e) -> Utils.printExceptionString("An unexpected exception happened in an event executor thread '" + t.getName() + "':", e));
		thread.setName("Event executor thread #" + eventThreadNumber++);

		return thread;
	});

	EventListenerImpl(BContextImpl context, Map<Class<?>, List<EventConsumer>> eventListeners) {
		this.context = context;
		this.eventListeners = eventListeners;
	}

	private void runCallback(EventConsumer consumer, Event event) {
		eventExecutor.execute(() -> {
			try {
				consumer.accept(event);
			} catch (Throwable e) {
				final ExceptionHandler handler = context.getUncaughtExceptionHandler();
				if (handler != null) {
					handler.onException(context, event, e);

					return;
				}

				Throwable baseEx = Utils.getException(e);

				Utils.printExceptionString("Unhandled exception in thread '" + Thread.currentThread().getName() + "' while executing an event", baseEx);

				context.dispatchException("Exception in component callback", baseEx);
			}
		});
	}

	@Override
	public void onEvent(@NotNull GenericEvent event) {
		if (!(event instanceof final Event realEvent)) return;

		final List<EventConsumer> eventConsumers = eventListeners.get(event.getClass());
		if (eventConsumers != null) {
			for (EventConsumer consumer : eventConsumers) {
				runCallback(consumer, realEvent);
			}
		}
	}
}
