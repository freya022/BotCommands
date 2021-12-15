package com.freya02.botcommands.internal.application;

import com.freya02.botcommands.api.Logging;
import com.freya02.botcommands.internal.BContextImpl;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.guild.GuildAvailableEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.member.update.GenericGuildMemberUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ApplicationUpdaterListener extends ListenerAdapter {
	private static final Logger LOGGER = Logging.getLogger();
	private final BContextImpl context;

	private final List<Long> updatedJoinedGuilds = new ArrayList<>();

	public ApplicationUpdaterListener(BContextImpl context) {
		this.context = context;
	}

	@Override
	public void onGuildAvailable(@NotNull GuildAvailableEvent event) {
		tryUpdate(event.getGuild());
	}

	@Override
	public void onGuildJoin(@NotNull GuildJoinEvent event) {
		tryUpdate(event.getGuild());
	}

	@Override
	public void onGenericGuildMemberUpdate(@NotNull GenericGuildMemberUpdateEvent event) {
		if (event.getMember().getIdLong() == event.getJDA().getSelfUser().getIdLong()) {
			tryUpdate(event.getGuild());
		}
	}

	private void tryUpdate(Guild guild) {
		synchronized (updatedJoinedGuilds) {
			if (updatedJoinedGuilds.contains(guild.getIdLong())) return;

			updatedJoinedGuilds.add(guild.getIdLong());
		}

		try {
			context.scheduleApplicationCommandsUpdate(Collections.singleton(guild));
		} catch (IOException e) {
			LOGGER.error("An error occurred while updating guild '{}' ({}) commands (on guild join / on unavailable guild join but became available later)", guild.getName(), guild.getIdLong(), e);
		}
	}
}
