package io.github.freya022.botcommands.test.commands.slash;

import io.github.freya022.botcommands.api.commands.application.ApplicationCommand;
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption;

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
