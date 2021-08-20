package com.freya02.bot.registeredclasses.othercommands;

import com.freya02.botcommands.annotation.CommandMarker;
import com.freya02.botcommands.annotation.ConditionalUse;
import com.freya02.botcommands.application.GuildSlashEvent;
import com.freya02.botcommands.application.SlashCommand;
import com.freya02.botcommands.application.slash.annotations.JdaSlashCommand;

@CommandMarker //Just so the class isn't marked as unused
public class WindowsCommand extends SlashCommand {
	//Here we'll only activate this command if the bot host is running on Windows, you might do that, for example, if you need a special program on it.
	@ConditionalUse
	public static boolean isUsable() {
		return System.getProperty("os.name").startsWith("Windows");
	}

	@JdaSlashCommand(name = "windows")
	public void run(GuildSlashEvent event) {
		event.reply("Windows:tm:")
				.setEphemeral(true)
				.queue();
	}
}