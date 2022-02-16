package com.freya02.botcommands.test.commands.slash;

import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;
import com.freya02.botcommands.api.modals.Modals;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.text.Modal;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;

public class SlashModal extends ApplicationCommand {
	@JDASlashCommand(
			name = "modal",
			description = "Test modal"
	)
	public void onSlashModal(GuildSlashEvent event) {
		final Modal modal = Modals.create("Formatting !")
				.addActionRows(ActionRow.of(
						TextInput.create("modal:formatting:code", "Java code", TextInputStyle.PARAGRAPH)
								.setRequired(false)
								.build()
				))
				.build();

		event.replyModal(modal).queue();
	}
}
