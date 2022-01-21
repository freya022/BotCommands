package com.freya02.bot.wiki.slash.commands;

import com.freya02.botcommands.api.Logging;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.CommandPath;
import com.freya02.botcommands.api.application.annotations.AppOption;
import com.freya02.botcommands.api.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.Command;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class SlashChoices extends ApplicationCommand {
	private static final Logger LOGGER = Logging.getLogger();

	private final List<Command.Choice> valueList = new ArrayList<>();

	@Override
	@NotNull
	public List<Command.Choice> getOptionChoices(@Nullable Guild guild, @NotNull CommandPath commandPath, int optionIndex) {
		if (optionIndex == 0) {
			return valueList;
		}

		return List.of();
	}

	@JDASlashCommand(name = "choices", subcommand = "choose")
	public void choose(GuildSlashEvent event,
	                   @AppOption(description = "The value you choose") String value) {
		event.reply("Your choice: " + value)
				.setEphemeral(true)
				.queue();
	}

	@JDASlashCommand(name = "choices", subcommand = "add")
	public void addChoice(GuildSlashEvent event,
	                   @AppOption(description = "The name of the choice") String name,
	                   @AppOption(description = "The value of the choice") String value) {
		event.deferReply(true).queue();

		valueList.add(new Command.Choice(name, value));

		//You should handle the exceptions inside the completable future, in case an error occurred
		event.getContext().scheduleApplicationCommandsUpdate(event.getGuild(), false, false);

		event.getHook().sendMessage("Choice added successfully").queue();
	}
}