package io.github.freya022.botcommands.test.commands.slash;

import io.github.freya022.botcommands.api.commands.application.ApplicationCommand;
import io.github.freya022.botcommands.api.commands.application.CommandScope;
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import io.github.freya022.botcommands.api.core.BContext;
import io.github.freya022.botcommands.api.modals.Modals;
import io.github.freya022.botcommands.api.modals.annotations.ModalData;
import io.github.freya022.botcommands.api.modals.annotations.ModalHandler;
import io.github.freya022.botcommands.api.modals.annotations.ModalInput;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;

import java.util.concurrent.TimeUnit;

public class SlashModal extends ApplicationCommand {
	private static final String MODAL_HANDLER_NAME = "test";
	private static final String CODE_INPUT_NAME = "code";

	@JDASlashCommand(
			scope = CommandScope.GLOBAL,
			name = "modal",
			description = "Test modal"
	)
	public void onSlashModal(GuildSlashEvent event, Modals modals) {
		final Modal modal = modals.create("Formatting !")
				.bindTo(MODAL_HANDLER_NAME, "foobar", 42L, null)
				.setTimeout(10, TimeUnit.SECONDS, () -> System.out.println("bruh"))
				.addActionRow(
						modals.createTextInput(CODE_INPUT_NAME, "Java code", TextInputStyle.PARAGRAPH)
								.setRequired(false)
								.build()
				)
				.build();

		event.replyModal(modal).queue();
	}

	@ModalHandler(name = MODAL_HANDLER_NAME)
	public void handle(ModalInteractionEvent event,
	                   @ModalData String test,
	                   @ModalData long number,
	                   @ModalData Object definitelyNull,
	                   @ModalInput(name = CODE_INPUT_NAME) String javaCode,
	                   BContext context) {
		event.reply(("""
						User data: %s
						Context: %s
						Definitely null: %s
						Your code:
						%s""").formatted(test + " = " + number, context, definitelyNull, javaCode))
				.setEphemeral(true)
				.queue();
	}
}
