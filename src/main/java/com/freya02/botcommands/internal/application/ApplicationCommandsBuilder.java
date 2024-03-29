package com.freya02.botcommands.internal.application;

import com.freya02.botcommands.api.Logging;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.CommandPath;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

public final class ApplicationCommandsBuilder {
	private static final Logger LOGGER = Logging.getLogger();
	private final ExecutorService es = Executors.newFixedThreadPool(Math.min(4, Runtime.getRuntime().availableProcessors()));
	private final BContextImpl context;
	private final List<Long> slashGuildIds;

	private final ReentrantLock globalLock = new ReentrantLock();
	private final Map<Long, ReentrantLock> lockMap = Collections.synchronizedMap(new HashMap<>());

	public ApplicationCommandsBuilder(@NotNull BContextImpl context, List<Long> slashGuildIds) {
		this.context = context;
		this.slashGuildIds = slashGuildIds;
		this.context.setSlashCommandsBuilder(this);
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
		ReflectionUtils.checkApplicationCommandParameter(method,
				method.getAnnotation(JDAUserCommand.class).scope(),
				GlobalUserEvent.class,
				GuildUserEvent.class);

		final UserCommandInfo info = new UserCommandInfo(context, applicationCommand, method);

		final CommandPath effectivePath = context.addUserCommand(info);
		LOGGER.debug("Added user command {} for method {}", effectivePath, Utils.formatMethodShort(method));
	}

	private void processMessageCommand(ApplicationCommand applicationCommand, Method method) {
		ReflectionUtils.checkApplicationCommandParameter(method,
				method.getAnnotation(JDAMessageCommand.class).scope(),
				GlobalMessageEvent.class,
				GuildMessageEvent.class);

		final MessageCommandInfo info = new MessageCommandInfo(context, applicationCommand, method);

		final CommandPath effectivePath = context.addMessageCommand(info);
		LOGGER.debug("Added message command {} for method {}", effectivePath, Utils.formatMethodShort(method));
	}

	private void processSlashCommand(ApplicationCommand applicationCommand, Method method) {
		ReflectionUtils.checkApplicationCommandParameter(method,
				method.getAnnotation(JDASlashCommand.class).scope(),
				GlobalSlashEvent.class,
				GuildSlashEvent.class);

		final SlashCommandInfo info = new SlashCommandInfo(context, applicationCommand, method);

		final CommandPath effectivePath = context.addSlashCommand(info);
		LOGGER.debug("Added slash command path {} for method {}", effectivePath, Utils.formatMethodShort(method));
	}

	private String getCheckTypeString() {
		if (context.isOnlineAppCommandCheckEnabled()) {
			return "Online check";
		} else {
			return "Local disk check";
		}
	}

	public void postProcess() throws IOException {
		context.getJDA().setRequiredScopes("applications.commands");

		context.setApplicationCommandsCache(new ApplicationCommandsCache(context));

		scheduleGlobalApplicationCommandsUpdate(false, context.isOnlineAppCommandCheckEnabled()).whenComplete((wasUpdated, e) -> {
			if (e != null) {
				LOGGER.error("An error occurred while updating global commands", e);
			}
		});

		final Map<Guild, CompletableFuture<CommandUpdateResult>> map;
		final ShardManager shardManager = context.getJDA().getShardManager();
		if (shardManager != null) {
			map = scheduleApplicationCommandsUpdate(shardManager.getGuildCache(), false, context.isOnlineAppCommandCheckEnabled());
		} else {
			map = scheduleApplicationCommandsUpdate(context.getJDA().getGuildCache(), false, context.isOnlineAppCommandCheckEnabled());
		}

		map.forEach((guild, future) -> {
			future.whenComplete((result, throwable) -> handleApplicationUpdateException(guild, throwable));
		});
	}

	public CompletableFuture<Boolean> scheduleGlobalApplicationCommandsUpdate(boolean force, boolean online) {
		return CompletableFuture.supplyAsync(() -> {
			globalLock.lock();
			try {
				final ApplicationCommandsUpdater globalUpdater = ApplicationCommandsUpdater.ofGlobal(context, online);
				final boolean shouldUpdateCommands = force || globalUpdater.shouldUpdateCommands();
				if (shouldUpdateCommands) {
					globalUpdater.updateCommands();
					LOGGER.debug("Global commands were updated ({})", getCheckTypeString());
				} else {
					LOGGER.debug("Global commands does not have to be updated ({})", getCheckTypeString());
				}

				context.getApplicationCommandsContext().putLiveApplicationCommandsMap(null, ApplicationCommandInfoMap.fromCommandList(globalUpdater.getScopeApplicationCommands()));

				return shouldUpdateCommands;
			} catch (Throwable e) {
				throw new RuntimeException("An exception occurred while updating global commands", e);
			} finally {
				globalLock.unlock();
			}
		}, es);
	}

	void handleApplicationUpdateException(Guild guild, Throwable throwable) {
		if (throwable != null) {
			ErrorResponseException e = Utils.getErrorResponseException(throwable);

			if (e != null && e.getErrorResponse() == ErrorResponse.MISSING_ACCESS) {
				final String inviteUrl = context.getJDA().getInviteUrl() + "&guild_id=" + guild.getId();

				LOGGER.warn("Could not register guild commands for guild '{}' ({}) as it appears the OAuth2 grants misses applications.commands, you can re-invite the bot in this guild with its already existing permission with this link: {}", guild.getName(), guild.getId(), inviteUrl);
				context.getRegistrationListeners().forEach(r -> r.onGuildSlashCommandMissingAccess(guild, inviteUrl));
			} else {
				LOGGER.error("Encountered an exception while updating commands for guild '{}' ({})", guild.getName(), guild.getId(), throwable);
			}
		}
	}

	@NotNull
	public Map<Guild, CompletableFuture<CommandUpdateResult>> scheduleApplicationCommandsUpdate(@NotNull Iterable<Guild> guilds, boolean force, boolean onlineCheck) {
		final Map<Guild, CompletableFuture<CommandUpdateResult>> map = new HashMap<>();

		for (Guild guild : guilds) {
			if (!slashGuildIds.isEmpty() && !slashGuildIds.contains(guild.getIdLong())) continue;

			map.put(guild, scheduleApplicationCommandsUpdate(guild, force, onlineCheck));
		}

		return map;
	}

	@NotNull
	public CompletableFuture<CommandUpdateResult> scheduleApplicationCommandsUpdate(Guild guild, boolean force, boolean onlineCheck) {
		if (!slashGuildIds.isEmpty() && !slashGuildIds.contains(guild.getIdLong()))
			return CompletableFuture.completedFuture(new CommandUpdateResult(guild, false));

		return CompletableFuture.supplyAsync(() -> {
			final ReentrantLock lock;
			synchronized (lockMap) {
				lock = lockMap.computeIfAbsent(guild.getIdLong(), x -> new ReentrantLock());
			}

			try {
				lock.lock();

				final ApplicationCommandsUpdater updater = ApplicationCommandsUpdater.ofGuild(context, guild, onlineCheck);

				boolean updatedCommands = false;

				if (force || updater.shouldUpdateCommands()) {
					updater.updateCommands();

					updatedCommands = true;

					LOGGER.debug("Guild '{}' ({}) commands were{} updated ({})", guild.getName(), guild.getId(), force ? " force" : "", getCheckTypeString());
				} else {
					LOGGER.debug("Guild '{}' ({}) commands does not have to be updated ({})", guild.getName(), guild.getId(), getCheckTypeString());
				}

				context.getApplicationCommandsContext().putLiveApplicationCommandsMap(guild, ApplicationCommandInfoMap.fromCommandList(updater.getScopeApplicationCommands()));

				return new CommandUpdateResult(guild, updatedCommands);
			} catch (Throwable e) {
				throw new RuntimeException("An exception occurred while updating guild commands for guild '" + guild.getName() + "' (" + guild.getId() + ")", e);
			} finally {
				lock.unlock();
			}
		}, es);
	}
}