package com.freya02.botcommands.api;

import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

/**
 * Similar to {@link ListenerAdapter}
 */
public abstract class ExceptionHandlerAdapter implements ExceptionHandler {
	public final void onException(BContext context, Event event, Throwable throwable) {
		if (event instanceof MessageReceivedEvent e) {
			handle(context, e, throwable);
		} else if (event instanceof SlashCommandInteractionEvent e) {
			handle(context, e, throwable);
		} else if (event instanceof MessageContextInteractionEvent e) {
			handle(context, e, throwable);
		} else if (event instanceof UserContextInteractionEvent e) {
			handle(context, e, throwable);
		} else if (event instanceof ButtonInteractionEvent e) {
			handle(context, e, throwable);
		} else if (event instanceof SelectMenuInteractionEvent e) {
			handle(context, e, throwable);
		} else if (event instanceof ModalInteractionEvent e) {
			handle(context, e, throwable);
		}

		handle(context, event, throwable);
	}

	public void handle(BContext context, Event event, Throwable throwable) {}

	public void handle(BContext context, MessageReceivedEvent event, Throwable throwable) {}

	public void handle(BContext context, SlashCommandInteractionEvent event, Throwable throwable) {}

	public void handle(BContext context, MessageContextInteractionEvent event, Throwable throwable) {}

	public void handle(BContext context, UserContextInteractionEvent event, Throwable throwable) {}

	public void handle(BContext context, ModalInteractionEvent event, Throwable throwable) {}

	public void handle(BContext context, ButtonInteractionEvent event, Throwable throwable) {}

	public void handle(BContext context, SelectMenuInteractionEvent event, Throwable throwable) {}
}
