package com.freya02.botcommands.internal.application;

import com.freya02.botcommands.api.Logging;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.CommandUpdateResult;
import com.freya02.botcommands.api.application.context.annotations.JDAMessageCommand;
import com.freya02.botcommands.api.application.context.annotations.JDAUserCommand;
import com.freya02.botcommands.api.application.context.message.GlobalMessageEvent;
import com.freya02.botcommands.api.application.context.message.GuildMessageEvent;
import com.freya02.botcommands.api.application.context.user.GlobalUserEvent;
import com.freya02.botcommands.api.application.context.user.GuildUserEvent;
import com.freya02.botcommands.api.application.slash.GlobalSlashEvent;
import com.freya02.botcommands.api.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;
import com.freya02.botcommands.internal.BContextImpl;
import com.freya02.botcommands.internal.application.context.message.MessageCommandInfo;
import com.freya02.botcommands.internal.application.context.user.UserCommandInfo;
import com.freya02.botcommands.internal.application.slash.SlashCommandInfo;
import com.freya02.botcommands.internal.utils.ReflectionUtils;
import com.freya02.botcommands.internal.utils.Utils;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class ApplicationCommandsBuilder {
	private static final Logger LOGGER = Logging.getLogger();
	private final ExecutorService es = Executors.newFixedThreadPool(Math.min(4, Runtime.getRuntime().availableProcessors()));
	private final BContextImpl context;
	private final List<Long> slashGuildIds;

	public ApplicationCommandsBuilder(@NotNull BContextImpl context, List<Long> slashGuildIds) {
		this.context = context;
		this.context.setSlashCommandsBuilder(this);
		this.slashGuildIds = slashGuildIds;
	}

	public void processApplicationCommand(ApplicationCommand applicationCommand, Method method) {
		try {
			if (method.isAnnotationPresent(JDASlashCommand.class)) {
				processSlashCommand(applicationCommand, method);
			} else if (method.isAnnotationPresent(JDAUserCommand.class)) {
				processUserCommand(applicationCommand, method);
			} else if (method.isAnnotationPresent(JDAMessageCommand.class)) {
				processMessageCommand(applicationCommand, method);
			}
		} catch (Exception e) {
			throw new RuntimeException("An exception occurred while processing application command at " + Utils.formatMethodShort(method), e);
		}
	}

	private void processUserCommand(ApplicationCommand applicationCommand, Method method) {
		if (method.getAnnotation(JDAUserCommand.class).guildOnly()) {
			if (!ReflectionUtils.hasFirstParameter(method, GlobalUserEvent.class) && !ReflectionUtils.hasFirstParameter(method, GuildUserEvent.class))
				throw new IllegalArgumentException("User command at " + Utils.formatMethodShort(method) + " must have a GuildUserEvent or GlobalUserEvent as first parameter");

			if (!ReflectionUtils.hasFirstParameter(method, GuildUserEvent.class)) {
				//If type is correct but guild specialization isn't used
				LOGGER.warn("Guild-only user command {} uses GlobalUserEvent, consider using GuildUserEvent to remove warnings related to guild stuff's nullability", Utils.formatMethodShort(method));
			}
		} else {
			if (!ReflectionUtils.hasFirstParameter(method, GlobalUserEvent.class))
				throw new IllegalArgumentException("User command at " + Utils.formatMethodShort(method) + " must have a GlobalUserEvent as first parameter");
		}

		final UserCommandInfo info = new UserCommandInfo(applicationCommand, method);

		LOGGER.debug("Adding user command {} for method {}", info.getPath().getName(), Utils.formatMethodShort(method));
		context.addUserCommand(info);
	}

	private void processMessageCommand(ApplicationCommand applicationCommand, Method method) {
		if (method.getAnnotation(JDAMessageCommand.class).guildOnly()) {
			if (!ReflectionUtils.hasFirstParameter(method, GlobalMessageEvent.class) && !ReflectionUtils.hasFirstParameter(method, GuildMessageEvent.class))
				throw new IllegalArgumentException("Message command at " + Utils.formatMethodShort(method) + " must have a GuildMessageEvent or GlobalMessageEvent as first parameter");

			if (!ReflectionUtils.hasFirstParameter(method, GuildMessageEvent.class)) {
				//If type is correct but guild specialization isn't used
				LOGGER.warn("Guild-only message command {} uses GlobalMessageEvent, consider using GuildMessageEvent to remove warnings related to guild stuff's nullability", Utils.formatMethodShort(method));
			}
		} else {
			if (!ReflectionUtils.hasFirstParameter(method, GlobalMessageEvent.class))
				throw new IllegalArgumentException("Message command at " + Utils.formatMethodShort(method) + " must have a GlobalMessageEvent as first parameter");
		}

		final MessageCommandInfo info = new MessageCommandInfo(applicationCommand, method);

		LOGGER.debug("Adding message command {} for method {}", info.getPath().getName(), Utils.formatMethodShort(method));
		context.addMessageCommand(info);
	}

	private void processSlashCommand(ApplicationCommand applicationCommand, Method method) {
		if (method.getAnnotation(JDASlashCommand.class).guildOnly()) {
			if (!ReflectionUtils.hasFirstParameter(method, GlobalSlashEvent.class) && !ReflectionUtils.hasFirstParameter(method, GuildSlashEvent.class))
				throw new IllegalArgumentException("Slash command at " + Utils.formatMethodShort(method) + " must have a GuildSlashEvent or GlobalSlashEvent as first parameter");

			if (!ReflectionUtils.hasFirstParameter(method, GuildSlashEvent.class)) {
				//If type is correct but guild specialization isn't used
				LOGGER.warn("Guild-only slash command {} uses GlobalSlashEvent, consider using GuildSlashEvent to remove warnings related to guild stuff's nullability", Utils.formatMethodShort(method));
			}
		} else {
			if (!ReflectionUtils.hasFirstParameter(method, GlobalSlashEvent.class))
				throw new IllegalArgumentException("Slash command at " + Utils.formatMethodShort(method) + " must have a GlobalSlashEvent as first parameter");
		}

		final SlashCommandInfo info = new SlashCommandInfo(applicationCommand, method);

		LOGGER.debug("Adding slash command path {} for method {}", info.getPath(), Utils.formatMethodShort(method));
		context.addSlashCommand(info);
	}

	public void postProcess() throws IOException {
		context.getJDA().setRequiredScopes("applications.commands");

		context.setApplicationCommandsCache(new ApplicationCommandsCache(context));

		final ApplicationCommandsUpdater globalUpdater = ApplicationCommandsUpdater.ofGlobal(context);
		if (globalUpdater.shouldUpdateCommands()) {
			globalUpdater.updateCommands();
			LOGGER.debug("Global commands were updated");
		} else {
			LOGGER.debug("Global commands does not have to be updated");
		}

		final Map<Guild, CompletableFuture<CommandUpdateResult>> map;
		final ShardManager shardManager = context.getJDA().getShardManager();
		if (shardManager != null) {
			map = scheduleApplicationCommandsUpdate(shardManager.getGuildCache());
		} else {
			map = scheduleApplicationCommandsUpdate(context.getJDA().getGuildCache());
		}

		map.forEach((guild, future) -> {
			future.whenComplete((result, throwable) -> {
				if (throwable != null) {
					ErrorResponseException e = Utils.getErrorResponseException(throwable);

					if (e != null && e.getErrorResponse() == ErrorResponse.MISSING_ACCESS) {
						final String inviteUrl = context.getJDA().getInviteUrl() + "&guild_id=" + guild.getId();

						LOGGER.warn("Could not register guild commands for guild '{}' ({}) as it appears the OAuth2 grants misses applications.commands, you can re-invite the bot in this guild with its already existing permission with this link: {}", guild.getName(), guild.getId(), inviteUrl);
						context.getRegistrationListeners().forEach(r -> r.onGuildSlashCommandMissingAccess(guild, inviteUrl));
					} else {
						LOGGER.error("Encountered an exception while updating commands for guild '{}' ({})", guild.getName(), guild.getId(), e);
					}
				}
			});
		});
	}

	@NotNull
	public Map<Guild, CompletableFuture<CommandUpdateResult>> scheduleApplicationCommandsUpdate(@NotNull Iterable<Guild> guilds) throws IOException {
		final Map<Guild, CompletableFuture<CommandUpdateResult>> map = new HashMap<>();

		for (Guild guild : guilds) {
			if (!slashGuildIds.isEmpty() && !slashGuildIds.contains(guild.getIdLong())) continue;

			map.put(guild, scheduleApplicationCommandsUpdate(guild));
		}

		return map;
	}

	@NotNull
	public CompletableFuture<CommandUpdateResult> scheduleApplicationCommandsUpdate(Guild guild) throws IOException {
		final ApplicationCommandsUpdater updater = ApplicationCommandsUpdater.ofGuild(context, guild);

		return CompletableFuture.supplyAsync(() -> {
			try {
				boolean updatedCommands = false, updatedPrivileges = false;

				if (updater.shouldUpdateCommands()) {
					updater.updateCommands().get();

					updatedCommands = true;

					LOGGER.debug("Guild '{}' ({}) commands were updated", guild.getName(), guild.getId());
				} else {
					LOGGER.debug("Guild '{}' ({}) commands does not have to be updated", guild.getName(), guild.getId());
				}

				if (updater.shouldUpdatePrivileges()) {
					updater.updatePrivileges();

					updatedPrivileges = true;

					LOGGER.debug("Guild '{}' ({}) commands privileges were updated", guild.getName(), guild.getId());
				} else {
					LOGGER.debug("Guild '{}' ({}) commands privileges does not have to be updated", guild.getName(), guild.getId());
				}

				return new CommandUpdateResult(guild, updatedCommands, updatedPrivileges);
			} catch (Throwable e) {
				throw new RuntimeException("An exception occurred while updating guild commands for guild '" + guild.getName() + "' (" + guild.getId() + ")", e);
			}
		}, es);
	}
}