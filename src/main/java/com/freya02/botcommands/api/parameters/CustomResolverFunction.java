package com.freya02.botcommands.api.parameters;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.internal.ExecutableInteractionInfo;
import net.dv8tion.jda.api.events.Event;
import org.jetbrains.annotations.NotNull;

/**
 * @param <R> Type of the returned custom object
 * @see #apply(BContext, ExecutableInteractionInfo, Event)
 */
public interface CustomResolverFunction<R> {
	/**
	 * Computes a custom object from this event
	 *
	 * @param context                   The {@link BContext} of this bot
	 * @param executableInteractionInfo Some information about the executable which triggered this resolver
	 *                                  <br>You try to use <code>instanceof</code> on this object to see what type of command info it could be
	 * @param event                     The event that triggered the execution of this resolver
	 * @return The custom object which got computed
	 */
	R apply(@NotNull BContext context, @NotNull ExecutableInteractionInfo executableInteractionInfo, @NotNull Event event);
}
