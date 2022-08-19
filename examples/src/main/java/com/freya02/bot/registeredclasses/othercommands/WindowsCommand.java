package com.freya02.bot.registeredclasses.othercommands;

import com.freya02.botcommands.api.annotations.CommandMarker;
import com.freya02.botcommands.api.annotations.ConditionalUse;
import com.freya02.botcommands.api.commands.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.commands.application.ApplicationCommand;
import com.freya02.botcommands.api.commands.application.slash.annotations.JDASlashCommand;

@CommandMarker //Just so the class isn't marked as unused
public class WindowsCommand extends ApplicationCommand {
	//Here we'll only activate this command if the bot host is running on Windows, you might do that, for example, if you need a special program on it.
	@ConditionalUse
	public static boolean isUsable() {
		return System.getProperty("os.name").startsWith("Windows");
	}

	@JDASlashCommand(name = "windows")
	public void run(GuildSlashEvent event) {
		event.reply("Windows:tm:")
				.setEphemeral(true)
				.queue();
	}
}