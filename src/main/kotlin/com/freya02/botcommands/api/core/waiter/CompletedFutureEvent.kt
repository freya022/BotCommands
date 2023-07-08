package com.freya02.botcommands.api.core.waiter

import net.dv8tion.jda.api.events.Event
import java.util.concurrent.Future

/**
 * Functional interface for [EventWaiterBuilder.setOnComplete],
 * provides a [Future] and **either** the event **or** an exception.
 *
 * @param T The JDA event being waited for
 */
fun interface CompletedFutureEvent<T : Event> {
    fun accept(future: Future<T>, e: T?, t: Throwable?)
}
