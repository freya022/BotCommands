package com.freya02.botcommands.test.commands.slash;

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

public class SlashModal extends ApplicationCommand {
	private static final String MODAL_HANDLER_NAME = "test";

	@JDASlashCommand(
			name = "modal",
			description = "Test modal"
	)
	public void onSlashModal(GuildSlashEvent event) {
		final Modal modal = Modals.create(MODAL_HANDLER_NAME, "foobar")
				.setTitle("Formatting !")
				.addActionRows(ActionRow.of(
						Modals.createTextInput("code", "Java code", TextInputStyle.PARAGRAPH)
								.setRequired(false)
								.build()
				))
				.build();

		event.replyModal(modal).queue();
	}

	@ModalHandler(name = MODAL_HANDLER_NAME)
	public void handle(ModalInteractionEvent event,
					   @ModalData String test,
	                   @ModalInput(name = "code") String javaCode) {

	}
}
