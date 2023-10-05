package io.github.freya022.botcommands.test.commands.slash;

import io.github.freya022.botcommands.api.commands.application.ApplicationCommand;
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.Length;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption;

public class SlashLength extends ApplicationCommand {
	@JDASlashCommand(name = "length")
	public void onSlashLength(GuildSlashEvent event, @SlashOption @Length(max = 6) String inviteCode) {
		event.reply("Invite code: " + inviteCode).queue();
	}
}