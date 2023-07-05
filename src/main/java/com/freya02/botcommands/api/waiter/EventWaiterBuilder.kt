package com.freya02.botcommands.api.waiter;

import net.dv8tion.jda.api.events.Event;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Builder for {@link EventWaiter}
 *
 * @param <T> Type of the JDA event to wait after
 */
public interface EventWaiterBuilder<T extends Event> {
    /**
     * Sets the timeout for this event waiter; this means the action will no longer be usable after the time has elapsed
     *
     * @param timeout     Amount of time before the timeout occurs
     * @param timeoutUnit Unit of time for the timeout (minutes / seconds / millis...)
     *
     * @return This builder for chaining convenience
     */
    @NotNull
    EventWaiterBuilder<T> setTimeout(long timeout, @NotNull TimeUnit timeoutUnit);

    /**
     * Adds a precondition to this event waiter; this means your actions won't be executed unless all your preconditions are met<br>
     * <b>You can have multiple preconditions</b>
     *
     * @param precondition The precondition to check on each event
     *
     * @return This builder for chaining convenience
     */
    @NotNull
    EventWaiterBuilder<T> addPrecondition(@NotNull Predicate<T> precondition);

    /**
     * Sets the consumer called after the event waiter has all its preconditions met
     * and the task has not timeout nor been canceled
     *
     * @param onSuccess The success consumer to call
     *
     * @return This builder for chaining convenience
     */
    @NotNull
    EventWaiterBuilder<T> setOnSuccess(@NotNull Consumer<T> onSuccess);

    /**
     * Sets the consumer called when the event waiter has expired due to a timeout
     *
     * @param onTimeout The timeout consumer to call
     *
     * @return This builder for chaining convenience
     */
    @NotNull
    EventWaiterBuilder<T> setOnTimeout(@NotNull Runnable onTimeout);

    /**
     * Sets the consumer called after the event waiter has been canceled
     *
     * @param onCancelled The cancellation consumer to call
     *
     * @return This builder for chaining convenience
     */
    @NotNull
    EventWaiterBuilder<T> setOnCancelled(@NotNull Runnable onCancelled);

    /**
     * Sets the consumer called after the event waiter has "completed,"
     * i.e., it has either been successfully run, or been canceled, or has been timeout
     *
     * @param onComplete The consumer to call when the waiter is completed
     *
     * @return This builder for chaining convenience
     */
    @NotNull
    EventWaiterBuilder<T> setOnComplete(@NotNull CompletedFutureEvent<T> onComplete);

    /**
     * Submits the event waiter to the event waiting queue and returns the corresponding future, <b>This operation is not blocking</b>
     *
     * @return The {@link Future} of this event waiter, can be canceled
     */
    @NotNull
    CompletableFuture<T> submit();

    /**
     * Submits the event waiter to the event waiting queue, waits for the event to arrive and returns the event, <b>This operation is blocking</b>
     *
     * @return The event specified in {@link EventWaiter#of(Class)}
     *
     * @throws CancellationException If your code has canceled the event waiter
     * @throws ExecutionException    If an exception occurred in the event waiter or in a callback
     * @throws InterruptedException  If this thread gets interrupted while waiting for the event
     */
    @NotNull
    T complete() throws CancellationException, ExecutionException, InterruptedException;
}