package com.freya02.botcommands.api.waiter;

import net.dv8tion.jda.api.events.GenericEvent;

import java.util.concurrent.Future;

/**
 * Just a TriConsumer for {@link EventWaiterBuilder#setOnComplete(CompletedFutureEvent)}, accepts a {@link Future} and the provided event or an exception<br>
 * <b>You will either receive the event object or a Throwable</b>
 *
 * @param <T> The JDA event waited for
 */
public interface CompletedFutureEvent<T extends GenericEvent> {
	void accept(Future<T> future, T e, Throwable t);
}
