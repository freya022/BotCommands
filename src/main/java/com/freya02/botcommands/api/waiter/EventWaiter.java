package com.freya02.botcommands.api.waiter;

import com.freya02.botcommands.api.Logging;
import com.freya02.botcommands.internal.utils.EventUtils;
import com.freya02.botcommands.internal.utils.Utils;
import com.freya02.botcommands.internal.waiter.WaitingEvent;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.hooks.SubscribeEvent;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * An event waiter - if you need to wait for an event to occur while not blocking threads or having listeners everywhere<br>
 * You provide the type of the JDA event you want to get<br>
 * You can then set properties such as preconditions, timeouts and actions to run when the event gets received / has an exception, etc...
 * <br>This event waiter cannot be constructed and does not need to be registered to the JDA instance, it is already done automatically
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
 * try { //Use this **only** if you need to block your thread waiting for the event, this is considered bad practise so rather use setOnSuccess / setonComplete
 * 	final GuildMessageReceivedEvent guildMessageReceivedEvent = future.get();
 * } catch (InterruptedException | ExecutionException e) {
 * 	e.printStackTrace();
 * }
 * </code></pre>
 */
public class EventWaiter implements EventListener {
	private static final Map<Class<? extends GenericEvent>, List<WaitingEvent<? extends GenericEvent>>> waitingMap = new HashMap<>();
	private static final Logger LOGGER = Logging.getLogger();
	private static final Object EVENT_LIST_LOCK = new Object();

	private static int commandThreadNumber = 0;
	private static final ExecutorService waiterCompleteService = Utils.createCommandPool(r -> {
		final Thread thread = new Thread(r);
		thread.setDaemon(false);
		thread.setUncaughtExceptionHandler((t, e) -> Utils.printExceptionString("An unexpected exception happened in an event waiter thread '" + t.getName() + "':", e));
		thread.setName("Event waiter thread #" + commandThreadNumber++);

		return thread;
	});

	private static JDA jda;
	private static EnumSet<GatewayIntent> intents = EnumSet.noneOf(GatewayIntent.class);
	private static boolean initialized;

	public EventWaiter(@NotNull JDA jda) {
		if (initialized)
			throw new IllegalStateException("Cannot build an EventWaiter more than once");

		initialized = true;

		LOGGER.debug("Initialized EventWaiter");

		EventWaiter.jda = jda;
		EventWaiter.intents = jda.getGatewayIntents();
	}

	/**
	 * Creates a new event waiter builder, waiting for the specified event to occur
	 *
	 * @param eventType The JDA event to wait after
	 * @param <T>       Type of the JDA event
	 * @return A new event waiter builder
	 */
	public static <T extends GenericEvent> EventWaiterBuilder<T> of(Class<T> eventType) {
		if (!initialized)
			throw new IllegalStateException("Framework must be constructed before using the EventWaiter");

		EventUtils.checkEvent(jda, intents, eventType);

		return new EventWaiterBuilder<>(eventType);
	}

	static <T extends GenericEvent> Future<T> submit(WaitingEvent<T> waitingEvent) {
		CompletableFuture<T> future = waitingEvent.getCompletableFuture();

		final List<WaitingEvent<?>> waitingEvents = getWaitingEventsByType(waitingEvent);
		if (waitingEvent.getTimeout() > 0) {
			future.orTimeout(waitingEvent.getTimeout(), waitingEvent.getTimeoutUnit());
		}

		future.whenCompleteAsync((t, throwable) -> {
			final CompletedFutureEvent<T> onComplete = waitingEvent.getOnComplete();
			if (onComplete != null) onComplete.accept(future, t, throwable);

			if (throwable instanceof TimeoutException) {
				final Runnable onTimeout = waitingEvent.getOnTimeout();
				if (onTimeout != null) onTimeout.run();

				synchronized (EVENT_LIST_LOCK) {
					waitingEvents.remove(waitingEvent); //Not removed automatically by Iterator#remove before this method is called
				}
			} else if (t != null) {
				final Consumer<T> onSuccess = waitingEvent.getOnSuccess();
				if (onSuccess != null) onSuccess.accept(t);
			} else if (future.isCancelled()) {
				synchronized (EVENT_LIST_LOCK) {
					waitingEvents.remove(waitingEvent); //Not removed automatically by Iterator#remove before this method is called
				}

				final Runnable onCancelled = waitingEvent.getOnCancelled();
				if (onCancelled != null) onCancelled.run();
			} else {
				LOGGER.warn("Unexpected object received in EventWaiter Future#whenCompleteAsync, please report this to devs");
			}
		}, waiterCompleteService);

		waitingEvents.add(waitingEvent);

		return future;
	}

	@NotNull
	private static <T extends GenericEvent> List<WaitingEvent<?>> getWaitingEventsByType(WaitingEvent<T> waitingEvent) {
		return waitingMap.computeIfAbsent(waitingEvent.getEventType(), x -> Collections.synchronizedList(new ArrayList<>()));
	}

	@SuppressWarnings("unchecked")
	@SubscribeEvent
	@Override
	public void onEvent(@NotNull GenericEvent event) {
		final List<WaitingEvent<? extends GenericEvent>> waitingEvents = waitingMap.get(event.getClass());

		if (waitingEvents != null) {
			synchronized (EVENT_LIST_LOCK) { //Prevent concurrent modification between iterator and on-timeout List#remove
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
						LOGGER.warn("Completable future was already completed somehow, please report to the dev");
					}
				}
			}
		}
	}
}
