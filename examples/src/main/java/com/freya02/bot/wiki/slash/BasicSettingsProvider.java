package com.freya02.bot.wiki.slash;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.CommandList;
import com.freya02.botcommands.api.Logging;
import com.freya02.botcommands.api.SettingsProvider;
import net.dv8tion.jda.api.entities.Guild;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BasicSettingsProvider implements SettingsProvider {
	private static final Logger LOGGER = Logging.getLogger();
	private final Map<Long, List<String>> disabledCommandsMap = new HashMap<>();
	private final BContext context;

	public BasicSettingsProvider(BContext context) {
		this.context = context;
	}

	@Override
	@NotNull
	public CommandList getGuildCommands(@NotNull Guild guild) {
		return CommandList.notOf(getBlacklist(guild));
	}

	@NotNull
	private List<String> getBlacklist(Guild guild) {
		//Blacklist filter - the ArrayList is created only if the guild's ID was not already in the map.
		return disabledCommandsMap.computeIfAbsent(guild.getIdLong(), x -> {
			final ArrayList<String> disabledCommands = new ArrayList<>();

			//Let's say the info command is disabled by default
			disabledCommands.add("info");

			return disabledCommands;
		});
	}

	//This is for the part where you want to update the command list later
	// So you can use this method to "enable" an application command for a guild
	// For example in a text command
	public void addCommand(Guild guild, String commandName) {
		getBlacklist(guild).remove(commandName); //Removes the command from the blacklist

		//You should handle the exceptions inside the completable future, in case an error occurred
		context.scheduleApplicationCommandsUpdate(guild, false, false);
	}
}
