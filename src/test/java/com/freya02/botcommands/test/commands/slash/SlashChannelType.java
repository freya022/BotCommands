package com.freya02.botcommands.test.commands.slash;

import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.annotations.AppOption;
import com.freya02.botcommands.api.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;
import net.dv8tion.jda.api.entities.Category;

public class SlashChannelType extends ApplicationCommand {
	@JDASlashCommand(name = "channeltype")
	public void run(GuildSlashEvent event, @AppOption Category channel) {
		event.reply("channel = " + channel).setEphemeral(true).queue();
	}
}
