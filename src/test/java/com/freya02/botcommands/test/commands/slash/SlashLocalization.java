package com.freya02.botcommands.test.commands.slash;

import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;

public class SlashLocalization extends ApplicationCommand {
	@JDASlashCommand(name = "localization")
	public void run(GuildSlashEvent event) {
		event.reply(event.localizeUser("Test", "commands.ban.name"))
				.setEphemeral(true)
				.queue();
	}
}
