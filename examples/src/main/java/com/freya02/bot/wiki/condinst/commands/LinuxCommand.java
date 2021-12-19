package com.freya02.bot.wiki.condinst.commands;

import com.freya02.botcommands.api.annotations.ConditionalUse;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;

public class LinuxCommand extends ApplicationCommand {
	@ConditionalUse //Called when the class is about to get constructed
	public static boolean canUse() { //Return false if it's not Linux
		final String osName = System.getProperty("os.name").toLowerCase();

		return osName.contains("linux") || osName.contains("nix"); //Not accurate but should do it, not tested
	}

	@JDASlashCommand(name = "linux")
	public void execute(GuildSlashEvent event) {
		event.reply("The bot runs on Linux").setEphemeral(true).queue();
	}
}
