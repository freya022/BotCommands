package com.freya02.botcommands.test.commands.slash;

import com.freya02.botcommands.api.commands.application.ApplicationCommand;
import com.freya02.botcommands.api.commands.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import com.freya02.botcommands.api.commands.application.slash.annotations.SlashOption;

public class SlashGroup extends ApplicationCommand {
	@JDASlashCommand(name = "anilist2", subcommand = "search", group = "user" ,description = "Searches anime")
	public void searchuser(
			GuildSlashEvent event,
			@SlashOption(description = "User to search") String anilistuser
	) {
		event.reply("ok").queue();
	}

	@JDASlashCommand(name = "anilist2", subcommand = "lookup", group = "user", description = "Lookups anilist user")
	public void lookupuser(
			GuildSlashEvent event,
			@SlashOption(description = "Anilist username") String username
	) {
		event.reply("ok").queue();
	}
}
