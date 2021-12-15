package com.freya02.botcommands.internal.application;

import com.freya02.botcommands.api.Logging;
import com.freya02.botcommands.internal.BContextImpl;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.guild.GuildAvailableEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ApplicationUpdaterListener extends ListenerAdapter {
	private static final Logger LOGGER = Logging.getLogger();

	private final BContextImpl context;
	private final Set<Long> failedGuilds = Collections.synchronizedSet(new HashSet<>());

	public ApplicationUpdaterListener(BContextImpl context) {
		this.context = context;
	}

	@Override
	public void onGuildAvailable(@NotNull GuildAvailableEvent event) {
		tryUpdate(event.getGuild(), true);
	}

	@Override
	public void onGuildJoin(@NotNull GuildJoinEvent event) {
		tryUpdate(event.getGuild(), true);
	}

	//Use this as a mean to detect OAuth scope changes
	@Override
	public void onGuildMemberUpdate(@NotNull GuildMemberUpdateEvent event) {
		if (event.getMember().getIdLong() == event.getJDA().getSelfUser().getIdLong()) {
			tryUpdate(event.getGuild(), false);
		}
	}

	private void tryUpdate(Guild guild, boolean force) {
		final boolean hadFailed = failedGuilds.remove(guild.getIdLong());

		context.getSlashCommandsBuilder()
				.scheduleApplicationCommandsUpdate(guild, force || hadFailed)
				.whenCompleteAsync((commandUpdateResult, e) -> {
			if (e != null) {
				failedGuilds.add(guild.getIdLong());

				context.getSlashCommandsBuilder().handleApplicationUpdateException(guild, e);
			}
		});
	}
}
