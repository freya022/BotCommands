package com.freya02.bot.wiki.localization.commands;

import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;
import com.freya02.botcommands.api.localization.annotations.LocalizationBundle;

public class SlashException extends ApplicationCommand {
	//Description is set in localization
	@JDASlashCommand(name = "exception")
	public void onSlashException(GuildSlashEvent event) {
		//Voluntary exception, the exception message will change depending on the user's language
		// There are strings for English & French, set in DefaultMessages.json & DefaultMessages_fr.json, in the /resources/bc_localization folder
		throw new UnsupportedOperationException();
	}
}
