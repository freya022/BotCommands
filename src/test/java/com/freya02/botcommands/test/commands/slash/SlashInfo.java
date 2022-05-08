package com.freya02.botcommands.test.commands.slash;

import com.freya02.botcommands.annotations.api.application.slash.annotations.JDASlashCommand;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.slash.GuildSlashEvent;

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