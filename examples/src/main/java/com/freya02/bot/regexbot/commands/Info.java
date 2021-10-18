package com.freya02.bot.regexbot.commands;

import com.freya02.botcommands.api.annotations.CommandMarker;
import com.freya02.botcommands.api.prefixed.BaseCommandEvent;
import com.freya02.botcommands.api.prefixed.TextCommand;
import com.freya02.botcommands.api.prefixed.annotations.Category;
import com.freya02.botcommands.api.prefixed.annotations.Description;
import com.freya02.botcommands.api.prefixed.annotations.JDATextCommand;
import com.freya02.botcommands.api.prefixed.annotations.TextOption;
import net.dv8tion.jda.api.entities.*;

@CommandMarker //No unused warnings
@Category("Utils")
@Description("Gives information about an entity")
public class Info extends TextCommand {
	//Specifying the order makes it so methods have priorities, this is useful in this command because TextChannel, Role and Guild might have the same ids
	// (example: @everyone, the first text channel created and the guild has the same id)
	@JDATextCommand(name = "info", order = 1) //Method to be checked first
	public void exec(BaseCommandEvent event,
	                 @TextOption Member member) { //@TextOption is mandatory on parameters that have to be parsed
		//Show member info
	}

	@JDATextCommand(name = "info", order = 2)
	public void exec(BaseCommandEvent event, @TextOption User user) {
		//Show user info
	}

	@JDATextCommand(name = "info", order = 3)
	public void exec(BaseCommandEvent event, @TextOption TextChannel channel) {
		//Show channel info
	}

	@JDATextCommand(name = "info", order = 4)
	public void exec(BaseCommandEvent event, @TextOption Role role) {
		//Show role info
	}

	@JDATextCommand(name = "info", order = 5) //Method to be checked last
	public void exec(BaseCommandEvent event, @TextOption Guild guild) {
		//Show guild info
	}
}
