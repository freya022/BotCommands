package com.freya02.botcommands.internal.core.waiter

import com.freya02.botcommands.api.waiter.CompletedFutureEvent
import com.freya02.botcommands.api.waiter.EventWaiterBuilder
import net.dv8tion.jda.api.events.Event
import net.dv8tion.jda.internal.utils.Checks
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import java.util.function.Consumer
import java.util.function.Predicate

internal class EventWaiterBuilderImpl<T : Event> internal constructor(
    private val eventWaiter: EventWaiterImpl,
    private val eventType: Class<T>
) : EventWaiterBuilder<T> {
    private val preconditions: MutableList<Predicate<T>> = arrayListOf()
    private var onSuccess: Consumer<T>? = null
    private var onTimeout: Runnable? = null
    private var onCancelled: Runnable? = null
    private var onComplete: CompletedFutureEvent<T>? = null

    private var timeout: Long? = null
    private var timeoutUnit: TimeUnit? = null

    override fun setTimeout(timeout: Long, timeoutUnit: TimeUnit): EventWaiterBuilder<T> = apply {
        Checks.positive(timeout, "timeout")
        this.timeout = timeout
        this.timeoutUnit = timeoutUnit
    }

    override fun setOnTimeout(onTimeout: Runnable): EventWaiterBuilder<T> = apply {
        this.onTimeout = onTimeout
    }

    override fun setOnCancelled(onCancelled: Runnable): EventWaiterBuilder<T> = apply {
        this.onCancelled = onCancelled
    }

    override fun setOnComplete(onComplete: CompletedFutureEvent<T>): EventWaiterBuilder<T> = apply {
        this.onComplete = onComplete
    }

    override fun setOnSuccess(onSuccess: Consumer<T>): EventWaiterBuilder<T> = apply {
        this.onSuccess = onSuccess
    }

    override fun addPrecondition(precondition: Predicate<T>): EventWaiterBuilder<T> = apply {
        this.preconditions += precondition
    }

    override fun submit(): CompletableFuture<T> = eventWaiter.submit(
        WaitingEvent(eventType, preconditions, onComplete, onSuccess, onTimeout, onCancelled, timeout, timeoutUnit)
    )

    override fun complete(): T = submit().get()
}