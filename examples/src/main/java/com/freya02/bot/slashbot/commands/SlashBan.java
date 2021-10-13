package com.freya02.bot.slashbot.commands;

import com.freya02.botcommands.api.annotations.BotPermissions;
import com.freya02.botcommands.api.annotations.CommandMarker;
import com.freya02.botcommands.api.annotations.Optional;
import com.freya02.botcommands.api.annotations.UserPermissions;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.annotations.AppOption;
import com.freya02.botcommands.api.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;

@BotPermissions(Permission.BAN_MEMBERS)
@UserPermissions(Permission.BAN_MEMBERS)
@CommandMarker //Just so the class isn't marked as unused
public class SlashBan extends ApplicationCommand { //Must extend SlashCommand
	@JDASlashCommand(
			//guild-only by default
			name = "ban",
			description = "Ban someone"
	)
	public void ban(GuildSlashEvent event,
	                @AppOption User user,
	                @Optional @AppOption(name = "del_days") long delDays, //Discord doesn't like uppercases
	                @Optional @AppOption String reason) {
		//check permissions and do ban logic

		event.replyFormat("Get banned %s, deleted %d days of messages, for reason `%s`", user.getAsMention(), delDays, reason).queue();
	}
}
