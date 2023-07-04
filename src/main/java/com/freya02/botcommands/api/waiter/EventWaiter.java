package com.freya02.botcommands.api.waiter;

import net.dv8tion.jda.api.events.Event;
import org.jetbrains.annotations.NotNull;

/**
 * An event waiter - if you need to wait for an event to occur while not blocking threads or having listeners everywhere.
 * <br>You provide the type of the JDA event you want to get
 * <br>You can then set properties such as preconditions, timeouts and actions to run when the event gets received / has an exception, etc...
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
public interface EventWaiter { //TODO convert to kt and add await
    /**
     * Creates a new event waiter builder, waiting for the specified event to occur
     *
     * @param eventType The JDA event to wait after
     * @param <T>       Type of the JDA event
     *
     * @return A new event waiter builder
     */
    <T extends Event> EventWaiterBuilder<T> of(@NotNull Class<T> eventType);
}
