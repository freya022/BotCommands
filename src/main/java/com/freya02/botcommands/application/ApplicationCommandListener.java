package com.freya02.botcommands.application;

import com.freya02.botcommands.BContextImpl;
import com.freya02.botcommands.Logging;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.guild.GuildAvailableEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ApplicationCommandListener extends ListenerAdapter {
	private static final Logger LOGGER = Logging.getLogger();
	private final BContextImpl context;

	private final List<Long> updatedJoinedGuilds = new ArrayList<>();

	public ApplicationCommandListener(BContextImpl context) {
		this.context = context;
	}

	@Override
	public void onGuildAvailable(@Nonnull GuildAvailableEvent event) {
		tryUpdate(event.getGuild());
	}

	@Override
	public void onGuildJoin(@Nonnull GuildJoinEvent event) {
		tryUpdate(event.getGuild());
	}

	private void tryUpdate(Guild guild) {
		synchronized (updatedJoinedGuilds) {
			if (updatedJoinedGuilds.contains(guild.getIdLong())) return;

			updatedJoinedGuilds.add(guild.getIdLong());
		}

		try {
			context.tryUpdateGuildCommands(Collections.singleton(guild));
		} catch (IOException e) {
			LOGGER.error("An error occurred while updating guild '{}' ({}) commands (on guild join / on unavailable guild join but became available later)", guild.getName(), guild.getIdLong(), e);
		}
	}
}
