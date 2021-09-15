package com.freya02.bot.slashbot.commands;

import com.freya02.botcommands.api.annotations.CommandMarker;
import com.freya02.botcommands.api.annotations.Optional;
import com.freya02.botcommands.api.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.slash.annotations.JdaSlashCommand;
import com.freya02.botcommands.api.application.annotations.Option;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;

@CommandMarker //Just so the class isn't marked as unused
public class SlashBan extends ApplicationCommand { //Must extend SlashCommand
	@JdaSlashCommand(
			//guild-only by default
			name = "ban",
			description = "Ban someone",
			botPermissions = Permission.BAN_MEMBERS,
			userPermissions = Permission.BAN_MEMBERS
	)
	public void ban(GuildSlashEvent event,
	                @Option User user,
	                @Optional @Option(name = "del_days") long delDays, //Discord doesn't like uppercases
	                @Optional @Option String reason) {
		//check permissions and do ban logic

		event.replyFormat("Get banned %s, deleted %d days of messages, for reason `%s`", user.getAsMention(), delDays, reason).queue();
	}
}
