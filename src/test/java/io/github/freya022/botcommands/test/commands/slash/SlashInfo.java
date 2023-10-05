package io.github.freya022.botcommands.test.commands.slash;

import io.github.freya022.botcommands.api.commands.application.ApplicationCommand;
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;

public class SlashInfo extends ApplicationCommand {
	@JDASlashCommand(name = "info", subcommand = "user")
	public void userInfo(GuildSlashEvent event) {
		//...
	}

	@JDASlashCommand(name = "info", subcommand = "channel")
	public void channelInfo(GuildSlashEvent event) {
		//...
	}

	@JDASlashCommand(name = "info", subcommand = "role")
	public void roleInfo(GuildSlashEvent event) {
		//...
	}
}