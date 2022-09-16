package com.freya02.bot.regexbot.commands;

import com.freya02.botcommands.api.annotations.BotPermissions;
import com.freya02.botcommands.api.annotations.CommandMarker;
import com.freya02.botcommands.api.annotations.Optional;
import com.freya02.botcommands.api.annotations.UserPermissions;
import com.freya02.botcommands.api.prefixed.BaseCommandEvent;
import com.freya02.botcommands.api.prefixed.TextCommand;
import com.freya02.botcommands.api.prefixed.annotations.Category;
import com.freya02.botcommands.api.prefixed.annotations.Description;
import com.freya02.botcommands.api.prefixed.annotations.JDATextCommand;
import com.freya02.botcommands.api.prefixed.annotations.TextOption;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;

@CommandMarker //No unused warnings
@Category("Moderation")
@Description("Kicks someone")
@BotPermissions(Permission.KICK_MEMBERS) //The bot need those permissions for others to use the command
@UserPermissions(Permission.KICK_MEMBERS) //The user need those permissions to use this command
public class Kick extends TextCommand {
	//This method gets executed if the command looks either like
	// !kick @freya02
	// or
	// !kick @freya02 get nae nae'd
	@JDATextCommand(name = "kick")
	public void exec(BaseCommandEvent event,
	                 @TextOption Member member,
	                 @TextOption(example = "Spam") /* In the help content this parameter will have 'Spam' as a reason example */ @Optional String reason) {
		event.getGuild().kick(member).reason(reason).queue();
	}
}
