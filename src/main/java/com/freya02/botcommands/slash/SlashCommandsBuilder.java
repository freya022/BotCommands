package com.freya02.botcommands.slash;

import com.freya02.botcommands.BContextImpl;
import com.freya02.botcommands.Logging;
import com.freya02.botcommands.Utils;
import com.freya02.botcommands.slash.annotations.JdaSlashCommand;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.internal.utils.tuple.ImmutablePair;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public final class SlashCommandsBuilder {
	private static final Logger LOGGER = Logging.getLogger();
	private final BContextImpl context;
	private final List<Long> slashGuildIds;

	public SlashCommandsBuilder(@NotNull BContextImpl context, List<Long> slashGuildIds) {
		this.context = context;
		this.context.setSlashCommandsBuilder(this);
		this.slashGuildIds = slashGuildIds;
	}

	public void processSlashCommand(SlashCommand slashCommand) {
		for (Method method : slashCommand.getClass().getDeclaredMethods()) {
			try {
				if (method.isAnnotationPresent(JdaSlashCommand.class)) {
					if (!method.canAccess(slashCommand))
						throw new IllegalStateException("Slash command " + method + " is not public");

					if (method.getAnnotation(JdaSlashCommand.class).guildOnly()) {
						if (!Utils.hasFirstParameter(method, SlashEvent.class) && !Utils.hasFirstParameter(method, GuildSlashEvent.class))
							throw new IllegalArgumentException("Slash command at " + method + " must have a GuildSlashEvent or SlashEvent as first parameter");

						if (!Utils.hasFirstParameter(method, GuildSlashEvent.class)) {
							//If type is correct but guild specialization isn't used
							LOGGER.warn("Guild-only command {} uses SlashEvent, consider using GuildSlashEvent to remove warnings related to guild stuff's nullability", method);
						}
					} else {
						if (!Utils.hasFirstParameter(method, SlashEvent.class))
							throw new IllegalArgumentException("Slash command at " + method + " must have a SlashEvent as first parameter");
					}

					final SlashCommandInfo info = new SlashCommandInfo(slashCommand, method);

					LOGGER.debug("Adding command path {} for method {}", info.getPath(), method);
					context.addSlashCommand(info.getPath(), info);
				}
			} catch (Exception e) {
				throw new RuntimeException("An exception occurred while processing slash command at " + method, e);
			}
		}
	}

	public void postProcess() throws IOException {
		context.getJDA().setRequiredScopes("applications.commands");

		context.setSlashCommandsCache(new SlashCommandsCache(context));

		final SlashCommandsUpdater globalUpdater = SlashCommandsUpdater.ofGlobal(context);
		if (globalUpdater.shouldUpdateCommands()) {
			globalUpdater.updateCommands();
			LOGGER.debug("Global commands were updated");
		} else {
			LOGGER.debug("Global commands does not have to be updated");
		}

		final List<Guild> guildCache;
		if (context.getJDA().getShardManager() != null) {
			guildCache = context.getJDA().getShardManager().getGuilds();
		} else {
			guildCache = context.getJDA().getGuilds();
		}

		tryUpdateGuildCommands(guildCache);
	}

	public boolean tryUpdateGuildCommands(Iterable<Guild> guilds) throws IOException {
		boolean changed = false;

		List<SlashCommandsUpdater> updaters = new ArrayList<>();
		for (Guild guild : guilds) {
			if (slashGuildIds.isEmpty() || slashGuildIds.contains(guild.getIdLong())) {
				updaters.add(SlashCommandsUpdater.ofGuild(context, guild));
			}
		}

		List<ImmutablePair<Guild, CompletableFuture<?>>> commandUpdatePairs = new ArrayList<>();
		for (SlashCommandsUpdater updater : updaters) {
			final Guild guild = updater.getGuild();

			if (updater.shouldUpdateCommands()) {
				changed = true;

				commandUpdatePairs.add(new ImmutablePair<>(guild, updater.updateCommands()));
				LOGGER.debug("Guild '{}' ({}) commands were updated", guild.getName(), guild.getId());
			} else {
				LOGGER.debug("Guild '{}' ({}) commands does not have to be updated", guild.getName(), guild.getId());
			}
		}

		final List<Long> missedGuilds = new ArrayList<>();
		for (ImmutablePair<Guild, CompletableFuture<?>> commandUpdatePair : commandUpdatePairs) {
			try {
				commandUpdatePair.getRight().join();
			} catch (CompletionException e) { // Check missing access exceptions
				if (e.getCause() instanceof ErrorResponseException) {
					if (((ErrorResponseException) e.getCause()).getErrorResponse() == ErrorResponse.MISSING_ACCESS) {
						final Guild guild = commandUpdatePair.getLeft();

						final String inviteUrl = context.getJDA().getInviteUrl() + "&guild_id=" + guild.getId();

						LOGGER.warn("Could not register guild commands for guild '{}' ({}) as it appears the OAuth2 grants misses applications.commands, you can re-invite the bot in this guild with its already existing permission with this link: {}", guild.getName(), guild.getId(), inviteUrl);
						context.getRegistrationListeners().forEach(r -> r.onGuildSlashCommandMissingAccess(guild, inviteUrl));

						missedGuilds.add(guild.getIdLong());

						continue;
					}
				}

				throw e;
			}
		}

		for (SlashCommandsUpdater updater : updaters) {
			final Guild guild = updater.getGuild();

			if (missedGuilds.contains(guild.getIdLong())) continue; //Missing the OAuth2 applications.commands scope in this guild

			if (updater.shouldUpdatePrivileges()) {
				changed = true;

				updater.updatePrivileges();
				LOGGER.debug("Guild '{}' ({}) commands privileges were updated", guild.getName(), guild.getId());
			} else {
				LOGGER.debug("Guild '{}' ({}) commands privileges does not have to be updated", guild.getName(), guild.getId());
			}
		}

		return changed;
	}
}