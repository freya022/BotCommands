package com.freya02.botcommands.test.commands.slash;

import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;
import com.freya02.botcommands.api.modals.Modals;
import com.freya02.botcommands.api.modals.annotations.ModalHandler;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.text.Modal;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;

public class SlashModal extends ApplicationCommand {
	private static final String MODAL_HANDLER_NAME = "test";

	@JDASlashCommand(
			name = "modal",
			description = "Test modal"
	)
	public void onSlashModal(GuildSlashEvent event) {
		final Modal modal = Modals.create(MODAL_HANDLER_NAME)
				.setTitle("Formatting !")
				.addActionRows(ActionRow.of(
						Modals.createTextInput("Java code", TextInputStyle.PARAGRAPH)
								.setRequired(false)
								.build()
				))
				.build();

		event.replyModal(modal).queue();
	}

	@ModalHandler(name = MODAL_HANDLER_NAME)
	public void handle(ModalInteractionEvent event) {

	}
}
