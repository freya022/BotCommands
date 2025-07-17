package dev.freya02.botcommands.jda.ktx

import io.github.freya022.botcommands.api.core.waiter.EventWaiter
import io.github.freya022.botcommands.api.core.waiter.EventWaiterBuilder
import net.dv8tion.jda.api.events.Event

/**
 * Same as [EventWaiter.of] but with a reified type parameter.
 */
inline fun <reified T : Event> EventWaiter.of(): EventWaiterBuilder<T> = of(T::class.java)
