package com.freya02.botcommands.test.commands.slash;

import com.freya02.botcommands.annotations.api.application.slash.annotations.JDASlashCommand;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.slash.GuildSlashEvent;

public class SlashBan extends ApplicationCommand {
//	@UserPermissions(Permission.BAN_MEMBERS)
	@JDASlashCommand(name = "ban", defaultLocked = true)
	public void ban(GuildSlashEvent event) {
		event.reply("ban")
				.setEphemeral(true)
				.queue();
	}
}
