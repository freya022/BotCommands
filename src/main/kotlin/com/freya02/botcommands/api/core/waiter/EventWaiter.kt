package com.freya02.botcommands.api.core.waiter

import com.freya02.botcommands.api.core.service.annotations.InjectedService
import net.dv8tion.jda.api.events.Event

/**
 * An event waiter - if you need to wait for a specific event to occur while not blocking threads or having listeners everywhere.
 *
 * You can set multiple preconditions, timeouts and actions to run when the event gets received / has an exception, etc...
 *
 * ### Example
 * This example uses every action, has a timeout of 1 second and only triggers if the caller is the same as the user who triggered the previously entered command
 *
 * ```java
 * final Future<MessageReceivedEvent> future = EventWaiter.of(MessageReceivedEvent.class)
 *     .setOnComplete((f, evt, throwable) -> System.out.println("Completed with an event " + evt + " or an exception " + throwable))
 *     .setOnTimeout(() -> System.err.println("Timeout"))
 *     .setOnSuccess(evt -> System.out.println("Success, received event: " + evt))
 *     .setOnCancelled(() -> System.err.println("Cancelled"))
 *     .setTimeout(1, TimeUnit.SECONDS)
 *     // Here "event" is the original event, such as a MessageReceivedEvent
 *     .addPrecondition(e -> e.getAuthor().getIdLong() == event.getAuthor().getIdLong())
 *     .submit();
 *
 * // If you want to cancel the event waiter
 * //future.cancel(true);
 *
 * // While I recommend to stick to setOnSuccess / setOnComplete,
 * // you can use this if you need to block while waiting for the event,
 * // but be aware that this might block a thread indefinitely if you're not careful.
 * try {
 *     final MessageReceivedEvent messageReceivedEvent = future.get();
 * } catch (InterruptedException | ExecutionException e) {
 *     LOGGER.error("Error waiting for event", e);
 * }
 * ```
 */
@InjectedService
interface EventWaiter {
    /**
     * Creates a new event waiter builder, waiting for the specified event to occur.
     *
     * @param eventType The JDA event to wait after
     * @param T         Type of the JDA event
     *
     * @return A new event waiter builder
     */
    fun <T : Event> of(eventType: Class<T>): EventWaiterBuilder<T>
}