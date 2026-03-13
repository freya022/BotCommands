package io.github.freya022.botcommands.api.core.waiter

import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService
import kotlinx.coroutines.future.await
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.UserSnowflake
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.events.Event
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

/**
 * Lets you run code when receiving a specific event, while not blocking threads nor having listeners everywhere,
 * and being able to capture existing variables.
 *
 * You can set multiple preconditions, timeouts and actions to run when the event gets received / has an exception, etc...
 *
 * You can also use [JDA.listenOnce] if you wish.
 *
 * ### Example
 * This example uses every action, has a timeout of 1 second and only triggers if the caller is the same as the user who triggered the previously entered command
 *
 * ```java
 * // Get the 'eventWaiter' instance by dependency injection
 * final Future<MessageReceivedEvent> future = eventWaiter.of(MessageReceivedEvent.class)
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
@InterfacedService(acceptMultiple = false)
interface EventWaiter {
    /**
     * Creates a new event waiter builder with the specified event type being awaited.
     *
     * @param eventType The JDA event to wait after
     * @param T         Type of the JDA event
     *
     * @throws IllegalArgumentException
     *         If the event is [RawGatewayEvent][net.dv8tion.jda.api.events.RawGatewayEvent]
     *         but [raw events][net.dv8tion.jda.api.JDABuilder.setRawEventsEnabled] were not enabled.
     *
     * @return A new event waiter builder
     */
    fun <T : Event> of(eventType: Class<T>): EventWaiterBuilder<T>
}

/**
 * Same as [EventWaiter.of] but with a reified type parameter.
 */
inline fun <reified T : Event> EventWaiter.of(): EventWaiterBuilder<T> = of(T::class.java)

/**
 * Suspends until an event of type [T] satisfying the [filter] is received on any shard, then returns it.
 *
 * If you wish to use a timeout with it, you can use [withTimeoutOrNull][kotlinx.coroutines.withTimeoutOrNull].
 *
 * @param filter Condition to satisfy before returning the event
 */
suspend inline fun <reified T : Event> EventWaiter.await(
    noinline filter: (T) -> Boolean = { true },
): T {
    return of<T>()
        .addPrecondition(filter)
        .submit().await()
}

/**
 * Suspends until a [Message] from the [author] satisfying the [filter] is received on any shard, then returns it.
 *
 * If you wish to use a timeout with it, you can use [withTimeoutOrNull][kotlinx.coroutines.withTimeoutOrNull].
 *
 * @param filter Additional conditions to satisfy before returning the message
 */
suspend inline fun EventWaiter.awaitMessage(
    channel: MessageChannel,
    author: UserSnowflake? = null,
    crossinline filter: (Message) -> Boolean = { true }
): Message {
    val channelId = channel.idLong
    val authorId = author?.idLong

    return await<MessageReceivedEvent> {
        it.channel.idLong == channelId
                && (authorId == null || it.author.idLong == authorId)
                && filter(it.message)
    }.message
}
