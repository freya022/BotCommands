package com.freya02.botcommands.test.commands.slash;

import com.freya02.botcommands.api.commands.application.ApplicationCommand;
import com.freya02.botcommands.api.commands.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import com.freya02.botcommands.api.commands.application.slash.annotations.Length;
import com.freya02.botcommands.api.commands.application.slash.annotations.SlashOption;

public class SlashLength extends ApplicationCommand {
	@JDASlashCommand(name = "length")
	public void onSlashLength(GuildSlashEvent event, @SlashOption @Length(max = 6) String inviteCode) {
		event.reply("Invite code: " + inviteCode).queue();
	}
}