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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Similar to {@link ListenerAdapter}
 */
public abstract class ExceptionHandlerAdapter implements ExceptionHandler {
	public final void onException(@NotNull BContext context, @Nullable Event event, @NotNull Throwable throwable) {
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

	public void handle(@NotNull BContext context, Event event, @NotNull Throwable throwable) {}

	public void handle(@NotNull BContext context, MessageReceivedEvent event, @NotNull Throwable throwable) {}

	public void handle(@NotNull BContext context, SlashCommandInteractionEvent event, @NotNull Throwable throwable) {}

	public void handle(@NotNull BContext context, MessageContextInteractionEvent event, @NotNull Throwable throwable) {}

	public void handle(@NotNull BContext context, UserContextInteractionEvent event, @NotNull Throwable throwable) {}

	public void handle(@NotNull BContext context, ModalInteractionEvent event, @NotNull Throwable throwable) {}

	public void handle(@NotNull BContext context, ButtonInteractionEvent event, @NotNull Throwable throwable) {}

	public void handle(@NotNull BContext context, SelectMenuInteractionEvent event, @NotNull Throwable throwable) {}
}
