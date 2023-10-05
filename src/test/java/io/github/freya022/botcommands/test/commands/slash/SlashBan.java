package io.github.freya022.botcommands.test.commands.slash;

import io.github.freya022.botcommands.api.commands.application.ApplicationCommand;
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;

public class SlashBan extends ApplicationCommand {
//	@UserPermissions(Permission.BAN_MEMBERS)
	@JDASlashCommand(name = "ban", defaultLocked = true)
	public void ban(GuildSlashEvent event) {
		event.reply("ban")
				.setEphemeral(true)
				.queue();
	}
}
