package com.freya02.bot.regexbot.commands;

import com.freya02.botcommands.BContext;
import com.freya02.botcommands.api.prefixed.BaseCommandEvent;
import com.freya02.botcommands.api.prefixed.Command;
import com.freya02.botcommands.api.prefixed.annotations.AddExecutableHelp;
import com.freya02.botcommands.api.prefixed.annotations.Executable;
import com.freya02.botcommands.api.prefixed.annotations.JdaCommand;
import net.dv8tion.jda.api.entities.*;

@AddExecutableHelp
@JdaCommand(
		name = "info",
		category = "Utils",
		description = "Gives information about an entity"
)
public class Info extends Command {
	public Info(BContext context) {
		super(context);
	}

	//Specifying the order makes it so methods have priorities, this is useful in this command because TextChannel, Role and Guild might have the same ids
	// (example: @everyone, the first text channel created and the guild has the same id)
	@Executable(order = 1) //Method to be checked first
	public void exec(BaseCommandEvent event, Member member) {
		//Show member info
	}

	@Executable(order = 2)
	public void exec(BaseCommandEvent event, User user) {
		//Show user info
	}

	@Executable(order = 3)
	public void exec(BaseCommandEvent event, TextChannel channel) {
		//Show channel info
	}

	@Executable(order = 4)
	public void exec(BaseCommandEvent event, Role role) {
		//Show role info
	}

	@Executable(order = 5) //Method to be checked last
	public void exec(BaseCommandEvent event, Guild guild) {
		//Show guild info
	}
}
