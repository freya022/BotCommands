package io.github.freya022.botcommands.api.core.waiter

import io.github.freya022.botcommands.api.core.config.BConfigBuilder
import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService
import net.dv8tion.jda.api.events.Event

/**
 * Lets you run code when receiving a specific event, while not blocking threads nor having listeners everywhere,
 * and being able to capture existing variables.
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
 *
 * @see BConfigBuilder.ignoredEventIntents
 */
@InterfacedService(acceptMultiple = false)
interface EventWaiter {
    /**
     * Creates a new event waiter builder with the specified event type being awaited.
     *
     * @param eventType The JDA event to wait after
     * @param T         Type of the JDA event
     *
     * @return A new event waiter builder
     */
    fun <T : Event> of(eventType: Class<T>): EventWaiterBuilder<T>
}