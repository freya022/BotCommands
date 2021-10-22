package com.freya02.bot.wiki.slash.commands;

import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.CommandPath;
import com.freya02.botcommands.api.application.annotations.AppOption;
import com.freya02.botcommands.api.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;
import com.freya02.botcommands.internal.Logging;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.SlashCommand;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SlashChoices extends ApplicationCommand {
	private static final Logger LOGGER = Logging.getLogger();

	private final List<SlashCommand.Choice> valueList = new ArrayList<>();

	@Override
	@NotNull
	public List<SlashCommand.Choice> getOptionChoices(@Nullable Guild guild, @NotNull CommandPath commandPath, int optionIndex) {
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

		valueList.add(new SlashCommand.Choice(name, value));

		try {
			event.getContext().tryUpdateGuildCommands(List.of(event.getGuild()));

			event.getHook().sendMessage("Choice added successfully").queue();
		} catch (IOException e) {
			LOGGER.error("Unable to update guild commands", e);

			event.getHook().sendMessage("Could not add the choice").queue();
		}
	}
}