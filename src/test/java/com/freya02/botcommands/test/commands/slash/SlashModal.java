package com.freya02.botcommands.test.commands.slash;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;
import com.freya02.botcommands.api.modals.Modals;
import com.freya02.botcommands.api.modals.annotations.ModalData;
import com.freya02.botcommands.api.modals.annotations.ModalHandler;
import com.freya02.botcommands.api.modals.annotations.ModalInput;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.text.Modal;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;

import java.util.concurrent.TimeUnit;

public class SlashModal extends ApplicationCommand {
	private static final String MODAL_HANDLER_NAME = "test";
	private static final String CODE_INPUT_NAME = "code";

	@JDASlashCommand(
			name = "modal",
			description = "Test modal"
	)
	public void onSlashModal(GuildSlashEvent event) {
		final Modal modal = Modals.create(MODAL_HANDLER_NAME, "foobar", 42L)
				.setTimeout(10, TimeUnit.SECONDS, () -> System.out.println("bruh"))
				.setTitle("Formatting !")
				.addActionRows(ActionRow.of(
						Modals.createTextInput(CODE_INPUT_NAME, "Java code", TextInputStyle.PARAGRAPH)
								.setRequired(false)
								.build()
				))
				.build();

		event.replyModal(modal).queue();
	}

	@ModalHandler(name = MODAL_HANDLER_NAME)
	public void handle(ModalInteractionEvent event,
	                   @ModalData String test,
	                   @ModalData long number,
	                   @ModalInput(name = CODE_INPUT_NAME) String javaCode,
	                   BContext context) {
		event.reply(("""
						User data: %s
						Context: %s
						Your code:
						%s""").formatted(test + " = " + number, context, javaCode))
				.setEphemeral(true)
				.queue();
	}
}
