package com.freya02.botcommands.test.commands.slash;

import com.freya02.botcommands.api.commands.application.ApplicationCommand;
import com.freya02.botcommands.api.commands.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.commands.application.slash.annotations.JDASlashCommand;

public class SlashBan extends ApplicationCommand {
//	@UserPermissions(Permission.BAN_MEMBERS)
	@JDASlashCommand(name = "ban", defaultLocked = true)
	public void ban(GuildSlashEvent event) {
		event.reply("ban")
				.setEphemeral(true)
				.queue();
	}
}
