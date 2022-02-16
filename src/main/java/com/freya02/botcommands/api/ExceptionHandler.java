package com.freya02.botcommands.api;

import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.hooks.EventListener;

/**
 * Similar to {@link EventListener}
 */
public interface ExceptionHandler {
	/**
	 * <b>Note: You are sent a generic Event, you will need to check it against SlashCommandInteractionEvent, GuildMessageReceivedEvent, etc... in order to differentiate events</b>
	 *
	 * @param context The current context
	 * @param event The event which triggered this exception
	 * @param throwable The throwable which got threw
	 */
	void onException(BContext context, Event event, Throwable throwable);
}
