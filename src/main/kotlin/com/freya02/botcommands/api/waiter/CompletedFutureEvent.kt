package com.freya02.botcommands.api.waiter

import net.dv8tion.jda.api.events.Event
import java.util.concurrent.Future

/**
 * Just a TriConsumer for [EventWaiterBuilder.setOnComplete],
 * provides a [Future] and **either** the event **or** an exception.
 *
 * @param T The JDA event waited for
 */
interface CompletedFutureEvent<T : Event> {
    fun accept(future: Future<T>, e: T?, t: Throwable?)
}
