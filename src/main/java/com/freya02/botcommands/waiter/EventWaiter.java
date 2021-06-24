package com.freya02.botcommands.waiter;

import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * An event waiter - if you need to wait for an event to occur while not blocking threads or having listeners everywhere<br>
 * You provide the type of the JDA event you want to get<br>
 * You can then set properties such as preconditions, timeouts and actions to run when the event gets received / has an exception, etc...
 *
 * <h2>Example</h2>
 * <h3>This example uses every actions, has a timeout of 1 second and only triggers if the caller is the same as the user who triggered the previously entered command</h3>
 * <pre><code>
 * final{@literal Future<GuildMessageReceivedEvent>} future = EventWaiter.of(GuildMessageReceivedEvent.class)
 * 		.setOnComplete((f, e, t){@literal ->} System.out.println("Completed"))
 * 		.setOnTimeout((){@literal ->} System.err.println("Timeout"))
 * 		.setOnSuccess(e{@literal ->} System.out.println("Success"))
 * 		.setOnCancelled((){@literal ->} System.err.println("Cancelled"))
 * 		.setTimeout(1, TimeUnit.SECONDS)
 * 		.addPrecondition(e{@literal ->} e.getAuthor().getIdLong() == event.getAuthor().getIdLong())
 * 		.submit();
 *
 * //future.cancel(true);
 *
 * try {
 * 	final GuildMessageReceivedEvent guildMessageReceivedEvent = future.get();
 * } catch (InterruptedException | ExecutionException e) {
 * 	e.printStackTrace();
 * }
 * </code></pre>
 */
public class EventWaiter extends ListenerAdapter {
	private static final Map<Class<? extends GenericEvent>, List<WaitingEvent<? extends GenericEvent>>> waitingMap = new HashMap<>();

	/**
	 * Creates a new event waiter builder, waiting for the specified event to occur
	 *
	 * @param eventType The JDA event to wait after
	 * @param <T>       Type of the JDA event
	 * @return A new event waiter builder
	 */
	public static <T extends GenericEvent> EventWaiterBuilder<T> of(Class<T> eventType) {
		return new EventWaiterBuilder<>(eventType);
	}

	static <T extends GenericEvent> Future<T> submit(WaitingEvent<T> waitingEvent) {
		CompletableFuture<T> future = waitingEvent.getCompletableFuture();

		final List<WaitingEvent<?>> waitingEvents = getWaitingEventsByType(waitingEvent);
		if (waitingEvent.getTimeout() > 0) {
			future.orTimeout(waitingEvent.getTimeout(), waitingEvent.getTimeoutUnit());
		}

		future.whenComplete((t, throwable) -> {
			final CompletedFutureEvent<T> onComplete = waitingEvent.getOnComplete();
			if (onComplete != null) onComplete.accept(future, t, throwable);

			if (throwable instanceof TimeoutException) {
				final Runnable onTimeout = waitingEvent.getOnTimeout();
				if (onTimeout != null) onTimeout.run();
			} else if (t != null) {
				final Consumer<T> onSuccess = waitingEvent.getOnSuccess();
				if (onSuccess != null) onSuccess.accept(t);
			} else if (future.isCancelled()) {
				final Runnable onCancelled = waitingEvent.getOnCancelled();
				if (onCancelled != null) onCancelled.run();
			} else {
				System.out.println("wut");
			}
		});

		waitingEvents.add(waitingEvent);

		return future;
	}

	@Nonnull
	private static <T extends GenericEvent> List<WaitingEvent<?>> getWaitingEventsByType(WaitingEvent<T> waitingEvent) {
		return waitingMap.computeIfAbsent(waitingEvent.getEventType(), x -> Collections.synchronizedList(new ArrayList<>()));
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onGenericEvent(@Nonnull GenericEvent event) {
		final List<WaitingEvent<? extends GenericEvent>> waitingEvents = waitingMap.get(event.getClass());

		if (waitingEvents != null) {
			eventLoop:
			for (Iterator<WaitingEvent<? extends GenericEvent>> iterator = waitingEvents.iterator(); iterator.hasNext(); ) {
				WaitingEvent<? extends GenericEvent> waitingEvent = iterator.next();

				for (Predicate<? extends GenericEvent> p : waitingEvent.getPreconditions()) {
					final Predicate<GenericEvent> precondition = (Predicate<GenericEvent>) p;

					if (!precondition.test(event)) {
						continue eventLoop;
					}
				}

				final CompletableFuture<GenericEvent> completableFuture = (CompletableFuture<GenericEvent>) waitingEvent.getCompletableFuture();
				if (completableFuture.complete(event)) {
					iterator.remove();
				} else {
					System.err.println("already completed");
				}
			}
		}
	}
}
