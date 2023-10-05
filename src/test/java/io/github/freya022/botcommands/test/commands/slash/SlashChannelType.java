package io.github.freya022.botcommands.test.commands.slash;

import io.github.freya022.botcommands.api.commands.application.ApplicationCommand;
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption;
import net.dv8tion.jda.api.entities.channel.concrete.Category;

public class SlashChannelType extends ApplicationCommand {
	@JDASlashCommand(name = "channeltype")
	public void run(GuildSlashEvent event, @SlashOption Category channel) {
		event.reply("channel = " + channel).setEphemeral(true).queue();
	}
}
