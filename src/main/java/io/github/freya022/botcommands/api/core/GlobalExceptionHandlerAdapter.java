package io.github.freya022.botcommands.api.core;

import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Adapter class for {@link GlobalExceptionHandler}.
 *
 * @see GlobalExceptionHandler
 */
public abstract class GlobalExceptionHandlerAdapter implements GlobalExceptionHandler {
	public final void onException(@Nullable Event event, @NotNull Throwable throwable) {
		if (event instanceof MessageReceivedEvent e) {
			handle(e, throwable);
		} else if (event instanceof SlashCommandInteractionEvent e) {
			handle(e, throwable);
		} else if (event instanceof MessageContextInteractionEvent e) {
			handle(e, throwable);
		} else if (event instanceof UserContextInteractionEvent e) {
			handle(e, throwable);
		} else if (event instanceof ButtonInteractionEvent e) {
			handle(e, throwable);
		} else if (event instanceof StringSelectInteractionEvent e) {
			handle(e, throwable);
		} else if (event instanceof EntitySelectInteractionEvent e) {
			handle(e, throwable);
		} else if (event instanceof ModalInteractionEvent e) {
			handle(e, throwable);
		}

		handle(event, throwable);
	}

	public void handle(@Nullable Event event, @NotNull Throwable throwable) {}

	public void handle(@NotNull MessageReceivedEvent event, @NotNull Throwable throwable) {}

	public void handle(@NotNull SlashCommandInteractionEvent event, @NotNull Throwable throwable) {}

	public void handle(@NotNull MessageContextInteractionEvent event, @NotNull Throwable throwable) {}

	public void handle(@NotNull UserContextInteractionEvent event, @NotNull Throwable throwable) {}

	public void handle(@NotNull ModalInteractionEvent event, @NotNull Throwable throwable) {}

	public void handle(@NotNull ButtonInteractionEvent event, @NotNull Throwable throwable) {}

	public void handle(@NotNull StringSelectInteractionEvent event, @NotNull Throwable throwable) {}

	public void handle(@NotNull EntitySelectInteractionEvent event, @NotNull Throwable throwable) {}
}
