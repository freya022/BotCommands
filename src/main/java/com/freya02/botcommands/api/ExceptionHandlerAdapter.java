package com.freya02.botcommands.api;

import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectionMenuEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

/**
 * Similar to {@link ListenerAdapter}
 */
public abstract class ExceptionHandlerAdapter implements ExceptionHandler {
	public final void onException(BContext context, Event event, Throwable throwable) {
		if (event instanceof MessageReceivedEvent e) {
			handle(context, e, throwable);
		} else if (event instanceof SlashCommandEvent e) {
			handle(context, e, throwable);
		} else if (event instanceof ButtonClickEvent e) {
			handle(context, e, throwable);
		} else if (event instanceof SelectionMenuEvent e) {
			handle(context, e, throwable);
		}

		handle(context, event, throwable);
	}

	public void handle(BContext context, Event event, Throwable throwable) {}

	public void handle(BContext context, MessageReceivedEvent event, Throwable throwable) {}

	public void handle(BContext context, SlashCommandEvent event, Throwable throwable) {}

	public void handle(BContext context, ButtonClickEvent event, Throwable throwable) {}

	public void handle(BContext context, SelectionMenuEvent event, Throwable throwable) {}
}
