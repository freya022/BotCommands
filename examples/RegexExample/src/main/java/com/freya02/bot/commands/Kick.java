package com.freya02.bot.commands;

import com.freya02.botcommands.BContext;
import com.freya02.botcommands.BaseCommandEvent;
import com.freya02.botcommands.Command;
import com.freya02.botcommands.annotation.*;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;

@AddExecutableHelp
@JdaCommand(
		name = "kick",
		category = "Moderation",
		description = "Kicks someone",
		botPermissions = Permission.KICK_MEMBERS, //The bot need those permissions to enable the command
		userPermissions = Permission.KICK_MEMBERS //The user need those permissions to use this command
)
public class Kick extends Command {
	protected Kick(BContext context) {
		super(context);
	}

	//This method gets executed if the command looks either like
	// ;kick @freya02
	// or
	// ;kick @freya02 get nae nae'd
	@Executable
	public void exec(BaseCommandEvent event,
	                 Member member,
	                 @ArgExample(str = "Spam") /* In the help content this parameter will have 'Spam' as a reason example */ @Optional String reason) {
		event.getGuild().kick(member, reason).queue();
	}
}
