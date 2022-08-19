package com.freya02.bot.wiki.condinst.commands;

import com.freya02.botcommands.api.annotations.ConditionalUse;
import com.freya02.botcommands.api.commands.application.ApplicationCommand;
import com.freya02.botcommands.api.commands.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.commands.application.slash.annotations.JDASlashCommand;

public class WindowsCommand extends ApplicationCommand {
	@ConditionalUse //Called when the class is about to get constructed
	public static boolean canUse() { //Return false if it's not Windows
		return System.getProperty("os.name").toLowerCase().contains("windows");
	}

	@JDASlashCommand(name = "windows")
	public void execute(GuildSlashEvent event) {
		event.reply("The bot runs on Windows").setEphemeral(true).queue();
	}
}
