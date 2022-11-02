package com.freya02.bot.wiki.slash.commands;

import com.freya02.botcommands.api.commands.application.ApplicationCommand;
import com.freya02.botcommands.api.commands.application.annotations.AppOption;
import com.freya02.botcommands.api.commands.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

public class SlashInfo extends ApplicationCommand {
	@JDASlashCommand(name = "info", subcommand = "user")
	public void userInfo(GuildSlashEvent event, @AppOption User user) {
		event.reply("User: " + user).queue();
	}

	@JDASlashCommand(name = "info", subcommand = "channel")
	public void channelInfo(GuildSlashEvent event, @AppOption TextChannel channel) {
		event.reply("Channel: " + channel).queue();
	}

	@JDASlashCommand(name = "info", subcommand = "role")
	public void roleInfo(GuildSlashEvent event, @AppOption Role role) {
		event.reply("Role: " + role).queue();
	}
}