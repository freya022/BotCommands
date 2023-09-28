package com.freya02.botcommands.internal.core.waiter

import com.freya02.botcommands.api.core.waiter.CompletedFutureEvent
import net.dv8tion.jda.api.events.Event
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer
import java.util.function.Predicate
import kotlin.time.Duration

internal class WaitingEvent<T : Event> internal constructor(
    val eventType: Class<T>,
    val preconditions: List<Predicate<T>>,
    val onComplete: CompletedFutureEvent<T>?,
    val onSuccess: Consumer<T>?,
    val onTimeout: Runnable?,
    val onCancelled: Runnable?,
    val timeout: Duration?
) {
    val completableFuture = CompletableFuture<T>()
}