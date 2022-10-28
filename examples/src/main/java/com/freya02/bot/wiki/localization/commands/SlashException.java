package com.freya02.bot.wiki.localization.commands;

import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;
import com.freya02.botcommands.api.localization.annotations.LocalizationBundle;

@LocalizationBundle("LocalizationWikiCommands")
public class SlashException extends ApplicationCommand {
	//Description is set in localization
	@JDASlashCommand(name = "exception")
	public void onSlashException(GuildSlashEvent event) {
		throw new UnsupportedOperationException();
	}
}
