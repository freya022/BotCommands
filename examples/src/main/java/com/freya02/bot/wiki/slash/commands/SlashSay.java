package com.freya02.bot.wiki.slash.commands;

import com.freya02.botcommands.api.commands.application.ApplicationCommand;
import com.freya02.botcommands.api.commands.application.CommandPath;
import com.freya02.botcommands.api.commands.application.annotations.AppOption;
import com.freya02.botcommands.api.commands.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.Command;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SlashSay extends ApplicationCommand {
	// If the method is placed in the same file then it is guaranteed to be only the "say" command path,
	// so it won't interfere with other commands
	@Override
	@NotNull
	public List<Command.Choice> getOptionChoices(@Nullable Guild guild, @NotNull CommandPath commandPath, int optionIndex) {
		if (optionIndex == 0) { //First option
			return List.of(
					//Only choices here are "Hi" and "Hello" and gets "translated" to their respective values
					new Command.Choice("Hi", "Greetings, comrad"),
					new Command.Choice("Hello", "Oy")
			);
		}

		return List.of();
	}

	@JDASlashCommand(
			//This command is guild-only by default
			name = "say",
			description = "Says what you type"
	)
	public void say(GuildSlashEvent event,
                    //Option name is by default the parameter name
                    @AppOption(description = "What you want to say") String text) {
		event.reply("Your choice: " + text).queue();
	}
}