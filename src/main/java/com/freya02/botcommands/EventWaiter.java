package com.freya02.botcommands;

import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

public class EventWaiter extends ListenerAdapter {
	private static final List<WaitingEvent<?>> waitingEvents = Collections.synchronizedList(new ArrayList<>());
	private static final ScheduledExecutorService timeoutService = Executors.newSingleThreadScheduledExecutor();

	private final BContextImpl context;

	private static final class WaitingEvent<T extends GenericEvent> {
		private final Class<T> type;
		private final @Nullable Predicate<T> precondition;
		private final EventRunnable<T> callable;

		private Future<?> future;

		public WaitingEvent(Class<T> type, @Nullable Predicate<T> precondition, EventRunnable<T> callable) {
			this.type = type;
			this.precondition = precondition;
			this.callable = callable;
		}

		public Class<T> getType() {
			return type;
		}

		@SuppressWarnings("unchecked")
		public boolean tryRun(GenericEvent event) throws Exception {
			if (precondition == null || precondition.test((T) event)) {
				future.cancel(false);
				callable.run((T) event);
				return true;
			} else {
				return false;
			}
		}

		public void setFuture(Future<?> future) {
			this.future = future;
		}
	}

	@FunctionalInterface
	public interface EventRunnable<T extends GenericEvent> {
		void run(T t) throws Exception;
	}

	private EventWaiter(BContextImpl context) {
		this.context = context;
	}

	static void createWaiter(BContextImpl context) {
		context.getJDA().addEventListener(new EventWaiter(context));
	}

	@Override
	public void onGenericEvent(@Nonnull GenericEvent event) {
		for (Iterator<WaitingEvent<?>> iterator = waitingEvents.iterator(); iterator.hasNext(); ) {
			WaitingEvent<?> waitingEvent = iterator.next();

			if (waitingEvent.getType().isAssignableFrom(event.getClass())) {
				try {
					if (waitingEvent.tryRun(event)) {
						iterator.remove();
					}
				} catch (Exception e) {
					final String msg = "An exception occurred while waiting for a " + waitingEvent.getType().getSimpleName();
					Utils.printExceptionString(msg, e);
					context.dispatchException(msg, e);
					iterator.remove();
				}
			}
		}
	}

	public static <T extends GenericEvent> void waitFor(Class<T> type, @Nullable Predicate<T> precondition, EventRunnable<T> callable, long timeout, TimeUnit unit) {
		WaitingEvent<T> e = new WaitingEvent<>(type, precondition, callable);

		e.setFuture(timeoutService.schedule(new Runnable() {
			{
				waitingEvents.add(e); //Keep the scheduler from counting down until the waiting event is pushed on the queue
			}

			@Override
			public void run() {
				waitingEvents.remove(e);
			}
		}, timeout, unit));
	}

	public static <T extends GenericEvent> void waitFor(Class<T> type, EventRunnable<T> callable, long timeout, TimeUnit unit) {
		waitFor(type, null, callable, timeout, unit);
	}

	public static <T extends GenericEvent> void waitFor(Class<T> type, @Nullable Predicate<T> precondition, EventRunnable<T> callable) {
		waitFor(type, precondition, callable, -1, TimeUnit.MILLISECONDS);
	}

	public static <T extends GenericEvent> void waitFor(Class<T> type, EventRunnable<T> callable) {
		waitFor(type, null, callable, -1, TimeUnit.MILLISECONDS);
	}
}
