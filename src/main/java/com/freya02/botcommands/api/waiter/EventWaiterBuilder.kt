package com.freya02.botcommands.api.waiter

import net.dv8tion.jda.api.events.Event
import java.util.concurrent.CancellationException
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import java.util.function.Consumer
import java.util.function.Predicate

/**
 * Builder for [EventWaiter]
 *
 * @param T Type of the JDA event to wait after
 */
interface EventWaiterBuilder<T : Event> {
    /**
     * Sets the timeout for this event waiter;
     * the action will no longer be usable after the time has elapsed.
     *
     * @param timeout     Amount of time before the timeout occurs
     * @param timeoutUnit Unit of time for the timeout (minutes / seconds / millis...)
     *
     * @throws IllegalArgumentException If the timeout is not positive
     *
     * @return This builder for chaining convenience
     */
    fun setTimeout(timeout: Long, timeoutUnit: TimeUnit): EventWaiterBuilder<T>

    /**
     * Adds a precondition to this event waiter;
     * the action won't be executed unless all your preconditions are met.
     *
     * @param precondition The precondition to check on each event
     *
     * @return This builder for chaining convenience
     */
    fun addPrecondition(precondition: Predicate<T>): EventWaiterBuilder<T>

    /**
     * Sets the consumer called after the event waiter has all its preconditions met
     * and the task has not timeout nor been canceled.
     *
     * @param onSuccess The success consumer to call
     *
     * @return This builder for chaining convenience
     */
    fun setOnSuccess(onSuccess: Consumer<T>): EventWaiterBuilder<T>

    /**
     * Sets the consumer called when the event waiter has expired due to a timeout.
     *
     * @param onTimeout The timeout consumer to call
     *
     * @return This builder for chaining convenience
     */
    fun setOnTimeout(onTimeout: Runnable): EventWaiterBuilder<T>

    /**
     * Sets the consumer called after the event waiter has been canceled.
     *
     * @param onCancelled The cancellation consumer to call
     *
     * @return This builder for chaining convenience
     */
    fun setOnCancelled(onCancelled: Runnable): EventWaiterBuilder<T>

    /**
     * Sets the consumer called after the event waiter has "completed,"
     * i.e., it has either been successfully run, or been canceled, or has been timeout.
     *
     * @param onComplete The consumer to call when the waiter is completed
     *
     * @return This builder for chaining convenience
     */
    fun setOnComplete(onComplete: CompletedFutureEvent<T>): EventWaiterBuilder<T>

    /**
     * Returns a [CompletableFuture] which is completed when the event waiter receives an event of the specified type,
     * and all [preconditions][addPrecondition] have passed.
     *
     * @return The [CompletableFuture] of this event waiter, can be canceled
     */
    fun submit(): CompletableFuture<T>

    /**
     * Blocks until the event waiter receives an event of the specified type,
     * and all [preconditions][addPrecondition] have passed.
     *
     * **Note**: I recommend you use [submit] with [CompletableFuture.whenComplete]
     *
     * @return The event specified in [EventWaiter.of]
     *
     * @throws CancellationException If you [canceled][CompletableFuture.cancel] the event waiter
     * @throws ExecutionException    If an exception occurred in the event waiter or in a callback
     * @throws InterruptedException  If this thread gets interrupted while waiting for the event
     */
    @Throws(CancellationException::class, ExecutionException::class, InterruptedException::class)
    fun complete(): T
}