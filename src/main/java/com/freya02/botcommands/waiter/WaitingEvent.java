package com.freya02.botcommands.waiter;

import net.dv8tion.jda.api.events.GenericEvent;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;

class WaitingEvent<T extends GenericEvent> {
	private final Class<T> eventType;

	private final List<Predicate<T>> preconditions;
	private final CompletedFutureEvent<T> onComplete;
	private final Consumer<T> onSuccess;
	private final Runnable onTimeout, onCancelled;

	private final long timeout;
	private final TimeUnit timeoutUnit;
	private final CompletableFuture<T> completableFuture = new CompletableFuture<>();

	WaitingEvent(Class<T> eventType, List<Predicate<T>> preconditions, CompletedFutureEvent<T> onComplete, Consumer<T> onSuccess, Runnable onTimeout, Runnable onCancelled, int timeout, TimeUnit timeoutUnit) {
		this.eventType = eventType;
		this.preconditions = preconditions;
		this.onComplete = onComplete;
		this.onSuccess = onSuccess;
		this.onTimeout = onTimeout;
		this.onCancelled = onCancelled;
		this.timeout = timeout;
		this.timeoutUnit = timeoutUnit;
	}

	CompletableFuture<T> getCompletableFuture() {
		return completableFuture;
	}

	Class<T> getEventType() {
		return eventType;
	}

	List<Predicate<T>> getPreconditions() {
		return preconditions;
	}

	Consumer<T> getOnSuccess() {
		return onSuccess;
	}

	Runnable getOnTimeout() {
		return onTimeout;
	}

	CompletedFutureEvent<T> getOnComplete() {
		return onComplete;
	}

	long getTimeout() {
		return timeout;
	}

	TimeUnit getTimeoutUnit() {
		return timeoutUnit;
	}

	public Runnable getOnCancelled() {
		return onCancelled;
	}
}
