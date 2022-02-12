package com.freya02.botcommands.api.waiter;

import com.freya02.botcommands.internal.waiter.WaitingEvent;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.internal.utils.Checks;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Builder for {@link EventWaiter}
 *
 * @param <T> Type of the JDA event to wait after
 */
public class EventWaiterBuilder<T extends GenericEvent> {
	private final Class<T> eventType;
	private final List<Predicate<T>> preconditions = new ArrayList<>();
	private Consumer<T> onSuccess;
	private Runnable onTimeout, onCancelled;
	private CompletedFutureEvent<T> onComplete;

	private int timeout;
	private TimeUnit timeoutUnit;

	EventWaiterBuilder(Class<T> eventType) {
		this.eventType = eventType;
	}

	/**
	 * Sets the timeout for this event waiter, this means the action will no longer be usable after the time has elapsed
	 *
	 * @param timeout     Amount of time before the timeout occurs
	 * @param timeoutUnit Unit of time for the timeout (minutes / seconds / millis...)
	 * @return This builder for chaining convenience
	 */
	public EventWaiterBuilder<T> setTimeout(int timeout, TimeUnit timeoutUnit) {
		Checks.positive(timeout, "timeout");

		this.timeout = timeout;
		this.timeoutUnit = timeoutUnit;

		return this;
	}

	/**
	 * Adds a precondition to this event waiter, this means your actions won't be executed unless all your preconditions are met<br>
	 * <b>You can have multiple preconditions</b>
	 *
	 * @param precondition The precondition to check on each event
	 * @return This builder for chaining convenience
	 */
	public EventWaiterBuilder<T> addPrecondition(Predicate<T> precondition) {
		Checks.notNull(precondition, "Event waiter precondition");

		this.preconditions.add(precondition);

		return this;
	}

	/**
	 * Sets the consumer called after the event waiter has all its preconditions met and the task has not timeout nor been cancelled
	 *
	 * @param onSuccess The success consumer to call
	 * @return This builder for chaining convenience
	 */
	public EventWaiterBuilder<T> setOnSuccess(Consumer<T> onSuccess) {
		this.onSuccess = onSuccess;

		return this;
	}

	/**
	 * Sets the consumer called when the event waiter has expired due to a timeout
	 *
	 * @param onTimeout The timeout consumer to call
	 * @return This builder for chaining convenience
	 */
	public EventWaiterBuilder<T> setOnTimeout(Runnable onTimeout) {
		this.onTimeout = onTimeout;

		return this;
	}

	/**
	 * Sets the consumer called after the event waiter has been cancelled
	 *
	 * @param onCancelled The cancellation consumer to call
	 * @return This builder for chaining convenience
	 */
	public EventWaiterBuilder<T> setOnCancelled(Runnable onCancelled) {
		this.onCancelled = onCancelled;

		return this;
	}

	/**
	 * Sets the consumer called after the event waiter has "completed", i.e. it has either been successfully ran, or been cancelled, or has been timeout
	 *
	 * @param onComplete The consumer to call when the waiter is completed
	 * @return This builder for chaining convenience
	 */
	public EventWaiterBuilder<T> setOnComplete(CompletedFutureEvent<T> onComplete) {
		this.onComplete = onComplete;

		return this;
	}

	/**
	 * Submits the event waiter to the event waiting queue and returns the corresponding future, <b>This operation is not blocking</b>
	 *
	 * @return The {@link Future} of this event waiter, can be cancelled
	 */
	public CompletableFuture<T> submit() {
		return EventWaiter.submit(new WaitingEvent<>(eventType, preconditions, onComplete, onSuccess, onTimeout, onCancelled, timeout, timeoutUnit));
	}

	/**
	 * Submits the event waiter to the event waiting queue, waits for the event to arrive and returns the event, <b>This operation is blocking</b>
	 *
	 * @return The event specified in {@link EventWaiter#of(Class)}
	 * @throws CancellationException If the event waiter has been cancelled by your code
	 * @throws ExecutionException    If an exception occured in the event waiter or in a callback
	 * @throws InterruptedException  If this thread gets interrupted while waiting for the event
	 */
	public T complete() throws CancellationException, ExecutionException, InterruptedException {
		return submit().get();
	}
}