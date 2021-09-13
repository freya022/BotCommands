package com.freya02.bot.slashbot.commands;

import com.freya02.botcommands.api.annotations.CommandMarker;
import com.freya02.botcommands.api.annotations.Optional;
import com.freya02.botcommands.api.application.GuildSlashEvent;
import com.freya02.botcommands.api.application.SlashCommand;
import com.freya02.botcommands.api.application.slash.annotations.JdaSlashCommand;
import com.freya02.botcommands.api.application.slash.annotations.Option;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;

@CommandMarker //Just so the class isn't marked as unused
public class SlashBan extends SlashCommand { //Must extend SlashCommand
	@JdaSlashCommand(
			//guild-only by default
			name = "ban",
			description = "Ban someone",
			botPermissions = Permission.BAN_MEMBERS,
			userPermissions = Permission.BAN_MEMBERS
	)
	public void ban(GuildSlashEvent event,
	                User user,
	                @Optional @Option(name = "del_days") Long delDays, //Discord doesn't like uppercases
	                @Optional String reason) {
		//check permissions and do ban logic

		event.replyFormat("Get banned %s, deleted %d days of messages, for reason `%s`", user.getAsMention(), delDays, reason).queue();
	}
}
