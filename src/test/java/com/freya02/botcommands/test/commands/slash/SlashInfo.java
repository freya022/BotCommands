package com.freya02.botcommands.test.commands.slash;

import com.freya02.botcommands.api.commands.application.ApplicationCommand;
import com.freya02.botcommands.api.commands.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.commands.application.slash.annotations.JDASlashCommand;

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