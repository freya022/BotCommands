package com.freya02.botcommands.application;

import com.freya02.botcommands.BGuildSettings;
import com.freya02.botcommands.SettingsProvider;
import com.freya02.botcommands.application.context.message.MessageCommandInfo;
import com.freya02.botcommands.application.context.user.UserCommandInfo;
import com.freya02.botcommands.application.slash.GuildApplicationSettings;
import com.freya02.botcommands.application.slash.LocalizedApplicationCommandData;
import com.freya02.botcommands.application.slash.SlashCommandInfo;
import com.freya02.botcommands.internal.BContextImpl;
import com.freya02.botcommands.internal.Logging;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.CommandType;
import net.dv8tion.jda.api.interactions.commands.SlashCommand;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import net.dv8tion.jda.api.interactions.commands.privileges.CommandPrivilege;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.freya02.botcommands.application.slash.SlashUtils.*;

public class ApplicationCommandsUpdater {
	private static final Logger LOGGER = Logging.getLogger();

	private final BContextImpl context;
	@Nullable private final Guild guild;

	private final Path commandsCachePath;
	private final Path privilegesCachePath;

	//TODO this might cause issues when a slash command and a context one have the same name
	private final Map<String, CommandData> map = new HashMap<>();
	private final Map<String, SubcommandGroupData> groupMap = new HashMap<>();

	private final List<String> ownerOnlyCommands = new ArrayList<>();
	private final List<Command> commands = new ArrayList<>();

	private final Map<String, String> localizedBaseNameToBaseName = new HashMap<>();
	private final Map<String, Collection<? extends CommandPrivilege>> cmdIdToPrivilegesMap = new HashMap<>();
	private final Map<String, Collection<? extends CommandPrivilege>> cmdBaseNameToPrivilegesMap = new HashMap<>();

	private ApplicationCommandsUpdater(@Nonnull BContextImpl context, @Nullable Guild guild) throws IOException {
		this.context = context;
		this.guild = guild;

		this.commandsCachePath = guild == null
				? context.getApplicationCommandsCache().getGlobalCommandsPath()
				: context.getApplicationCommandsCache().getGuildCommandsPath(guild);

		Files.createDirectories(commandsCachePath.getParent());

		if (guild == null) {
			this.privilegesCachePath = null;
		} else {
			this.privilegesCachePath = context.getApplicationCommandsCache().getGuildPrivilegesPath(guild);

			Files.createDirectories(privilegesCachePath.getParent());
		}

		computeCommands(context);
	}

	public static ApplicationCommandsUpdater ofGlobal(@Nonnull BContextImpl context) throws IOException {
		return new ApplicationCommandsUpdater(context, null);
	}

	public static ApplicationCommandsUpdater ofGuild(@Nonnull BContextImpl context, @Nonnull Guild guild) throws IOException {
		return new ApplicationCommandsUpdater(context, guild);
	}

	@Nullable
	public Guild getGuild() {
		return guild;
	}

	public boolean shouldUpdateCommands() throws IOException {
		if (Files.notExists(commandsCachePath)) return true;

		final byte[] oldBytes = Files.readAllBytes(commandsCachePath);
		final byte[] newBytes = ApplicationCommandsCache.getCommandsBytes(map.values());

		return !Arrays.equals(oldBytes, newBytes);
	}

	public CompletableFuture<?> updateCommands() {
		final Collection<CommandData> commandData = map.values();

		final CommandListUpdateAction updateAction = guild != null ? guild.updateCommands() : context.getJDA().updateCommands();

		final CompletableFuture<List<Command>> future = updateAction
				.addCommands(commandData)
				.submit();

		return guild != null ? thenAcceptGuild(commandData, future, guild) : thenAcceptGlobal(commandData, future);
	}

	public boolean shouldUpdatePrivileges() throws IOException {
		if (guild == null) return false;

		if (Files.notExists(privilegesCachePath)) return true;

		final byte[] oldBytes = Files.readAllBytes(privilegesCachePath);
		final byte[] newBytes = ApplicationCommandsCache.getPrivilegesBytes(cmdBaseNameToPrivilegesMap);

		return !Arrays.equals(oldBytes, newBytes);
	}

	public CompletableFuture<?> updatePrivileges() {
		if (guild == null) {
			return CompletableFuture.completedFuture(null);
		}

		if (commands.isEmpty()) {
			LOGGER.info("Privileges has changed but commands were not updated, retrieving current command list");
			
			return guild.retrieveCommands().submit().thenApply(retrievedCommands -> updatePrivileges0(guild, retrievedCommands));
		} else {
			return updatePrivileges0(guild, commands);
		}
	}

	private CompletableFuture<?> updatePrivileges0(@Nonnull Guild guild, @Nonnull List<Command> commands) {
		for (Command command : commands) {
			final Collection<? extends CommandPrivilege> privileges = cmdBaseNameToPrivilegesMap.get(localizedBaseNameToBaseName.get(command.getName()));

			if (privileges != null) {
				cmdIdToPrivilegesMap.put(command.getId(), privileges);
			}
		}

		return guild.updateCommandPrivileges(cmdIdToPrivilegesMap).submit().thenAccept(privilegesMap -> {
			try {
				Files.write(privilegesCachePath, ApplicationCommandsCache.getPrivilegesBytes(cmdIdToPrivilegesMap), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
			} catch (IOException e) {
				LOGGER.error("An exception occurred while temporarily saving guild ({} ({})) command privileges", guild.getName(), guild.getId(), e);
			}
		});
	}

	private void computeCommands(@Nonnull BContextImpl context) {
		final List<ApplicationCommandInfo> guildApplicationCommands = context.getApplicationCommands().stream()
				.filter(info -> {
					if (info.isGuildOnly() && guild == null) { //Do not update guild-only commands in global context
						return false;
					} else if (!info.isGuildOnly() && guild != null) { //Do not update global commands in guild context
						return false;
					}

					//Get the actual usable commands in this context (dm or guild)
					if (guild == null) return true;

					final BGuildSettings guildSettings = context.getGuildSettings(guild.getIdLong());
					if (guildSettings == null) return true; //If no specific guild settings, assume it's not filtered
					
					return guildSettings.getGuildCommands().getFilter().test(info.getPath());
				})
				.sorted(Comparator.comparingInt(ApplicationCommandInfo::getPathComponents))
				.collect(Collectors.toCollection(ArrayList::new));

		computeSlashCommands(context, guildApplicationCommands);

		computeContextCommands(guildApplicationCommands, UserCommandInfo.class, CommandType.USER_CONTEXT);

		computeContextCommands(guildApplicationCommands, MessageCommandInfo.class, CommandType.MESSAGE_CONTEXT);
		
		if (guild != null) {
			computePrivileges(guildApplicationCommands, guild);
		}
	}

	private void computeSlashCommands(@Nonnull BContextImpl context, List<ApplicationCommandInfo> guildApplicationCommands) {
		guildApplicationCommands.stream()
				.filter(a -> a instanceof SlashCommandInfo)
				.map(a -> (SlashCommandInfo) a)
				.forEachOrdered(info -> {
			try {
				final List<String> optionNames = getMethodOptionNames(info);
				final LocalizedApplicationCommandData localizedCommandData = getLocalizedCommandData(info, optionNames);
				
				//Put localized option names in order to resolve them when called
				if (guild != null) {
					info.putLocalizedOptions(guild.getIdLong(), optionNames);
				}

				localizedBaseNameToBaseName.put(getPathBase(getLocalizedPath(info, localizedCommandData)), info.getBaseName());

				final List<OptionData> methodOptions = getMethodOptions(info, localizedCommandData);

				final String path = getLocalizedPath(info, localizedCommandData);
				final String description = getLocalizedDescription(info, localizedCommandData);
				
				if (info.getPathComponents() == 1) {
					//Standard command
					final CommandData rightCommand = new CommandData(path, description);
					map.put(path, rightCommand);

					rightCommand.addOptions(methodOptions);

					if (info.isOwnerOnly()) {
						rightCommand.setDefaultEnabled(false);
					}
				} else if (info.getPathComponents() == 2) {
					//Subcommand of a command
					final String parent = getPathParent(path);
					final CommandData commandData = map.computeIfAbsent(parent, s -> {
						final CommandData tmpData = new CommandData(getPathName(parent), ".");
						if (info.isOwnerOnly()) {
							tmpData.setDefaultEnabled(false);
						}

						return tmpData;
					});

					final SubcommandData rightCommand = new SubcommandData(getPathName(path), description);
					commandData.addSubcommands(rightCommand);

					rightCommand.addOptions(methodOptions);
				} else if (info.getPathComponents() == 3) {
					final String namePath = getPathParent(getPathParent(path));
					final String parentPath = getPathParent(path);
					final SubcommandGroupData groupData = groupMap.computeIfAbsent(parentPath, gp -> {
						final CommandData nameData = new CommandData(getPathName(namePath), ".");
						map.put(getPathName(namePath), nameData);

						if (info.isOwnerOnly()) {
							nameData.setDefaultEnabled(false);
						}

						final SubcommandGroupData groupDataTmp = new SubcommandGroupData(getPathName(parentPath), ".");
						nameData.addSubcommandGroups(groupDataTmp);

						return groupDataTmp;
					});

					final SubcommandData rightCommand = new SubcommandData(getPathName(path), description);
					groupData.addSubcommands(rightCommand);

					rightCommand.addOptions(methodOptions);
				} else {
					throw new IllegalStateException("A slash command with more than 4 path components got registered");
				}

				context.addSlashCommandAlternative(path, info);

				if (!info.isOwnerOnly()) {
					if (ownerOnlyCommands.contains(info.getBaseName())) {
						LOGGER.warn("Non owner-only command '{}' is registered as a owner-only command because of another command with the same base name '{}'", info.getPath(), info.getBaseName());
					}
				}

				if (info.isOwnerOnly()) {
					if (info.isGuildOnly()) {
						ownerOnlyCommands.add(info.getBaseName());
					} else {
						LOGGER.warn("Owner-only command '{}' cannot be owner-only as it is a global command", info.getPath());
					}
				}
			} catch (Exception e) {
				throw new RuntimeException("An exception occurred while processing command " + info.getPath(), e);
			}
		});
	}
	
	@SuppressWarnings("unchecked")
	private <T extends ApplicationCommandInfo> void computeContextCommands(List<ApplicationCommandInfo> guildApplicationCommands, Class<T> targetClazz, CommandType type) {
		guildApplicationCommands.stream()
				.filter(a -> targetClazz.isAssignableFrom(a.getClass()))
				.map(a -> (T) a)
				.forEachOrdered(info -> {
					try {
						final LocalizedApplicationCommandData localizedCommandData = getLocalizedCommandData(info, null);

						localizedBaseNameToBaseName.put(getPathBase(getLocalizedPath(info, localizedCommandData)), info.getBaseName());

						// User command name
						final String path = getLocalizedPath(info, localizedCommandData);

						if (info.getPathComponents() == 1) {
							//Standard command
							final CommandData rightCommand = new CommandData(type, path);
							map.put(path, rightCommand);

							if (info.isOwnerOnly()) {
								rightCommand.setDefaultEnabled(false);
							}

							ownerOnlyCommands.add(info.getBaseName()); //Must be non-localized name
						} else {
							throw new IllegalStateException("A " + type.name() + " command with more than 1 path component got registered");
						}
					} catch (Exception e) {
						throw new RuntimeException("An exception occurred while processing a " + type.name() + " command " + info.getPath(), e);
					}
				});
	}

	@Nullable
	private LocalizedApplicationCommandData getLocalizedCommandData(ApplicationCommandInfo info, @Nullable List<String> optionNames) {
		final GuildApplicationSettings instance = info.getInstance();
		final LocalizedApplicationCommandData localizedCommandData = instance.getLocalizedCommandData(guild, info.getPath(), optionNames);

		if (localizedCommandData == null) {
			final SettingsProvider settingsProvider = context.getSettingsProvider();
			
			if (settingsProvider != null) {
				return settingsProvider.getLocalizedCommandData(guild, info.getPath(), optionNames);
			}
		}
		
		return localizedCommandData;
	}

	@Nonnull
	private List<CommandPrivilege> getCommandPrivileges(@Nonnull Guild guild, @Nonnull ApplicationCommandInfo info) {
		final List<CommandPrivilege> instancePrivileges = info.getInstance().getCommandPrivileges(guild, info.getBaseName());
		if (!instancePrivileges.isEmpty()) {
			return instancePrivileges;
		} else {
			final SettingsProvider settingsProvider = context.getSettingsProvider();

			if (settingsProvider != null) {
				return settingsProvider.getCommandPrivileges(guild, info.getBaseName());
			}
		}
		
		return List.of();
	}

	private CompletableFuture<?> thenAcceptGuild(Collection<CommandData> commandData, CompletableFuture<List<Command>> future, @Nonnull Guild guild) {
		return future.thenAccept(commands -> {
			for (Command command : commands) {
				if (command instanceof SlashCommand) {
					context.getRegistrationListeners().forEach(l -> l.onGuildSlashCommandRegistered(this.guild, (SlashCommand) command));
				}
			}

			this.commands.addAll(commands);

			try {
				Files.write(commandsCachePath, ApplicationCommandsCache.getCommandsBytes(commandData), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
			} catch (IOException e) {
				LOGGER.error("An exception occurred while temporarily saving guild ({} ({})) commands in '{}'", guild.getName(), guild.getId(), commandsCachePath.toAbsolutePath(), e);
			}

			if (!LOGGER.isTraceEnabled()) return;

			final StringBuilder sb = new StringBuilder("Updated commands for ");
			sb.append(guild.getName()).append(" :\n");
			appendCommands(commands, sb);

			LOGGER.trace(sb.toString().trim());
		});
	}

	private CompletableFuture<?> thenAcceptGlobal(Collection<CommandData> commandData, CompletableFuture<List<Command>> future) {
		return future.thenAccept(commands -> {
			for (Command command : commands) {
				if (command instanceof SlashCommand) {
					context.getRegistrationListeners().forEach(l -> l.onGlobalSlashCommandRegistered((SlashCommand) command));
				}
			}

			try {
				Files.write(commandsCachePath, ApplicationCommandsCache.getCommandsBytes(commandData), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
			} catch (IOException e) {
				LOGGER.error("An exception occurred while temporarily saving {} commands in '{}'", guild == null ? "global" : String.format("guild '%s' (%s)", guild.getName(), guild.getId()), commandsCachePath.toAbsolutePath(), e);
			}

			if (!LOGGER.isTraceEnabled()) return;

			final StringBuilder sb = new StringBuilder("Updated global commands:\n");
			appendCommands(commands, sb);

			LOGGER.trace(sb.toString().trim());
		});
	}

	//See how to integrate this step on the command build in order to use localized base names instead of resolving the not-localized one
	//Could do it here but would result in a 2nd localized data call
	private void computePrivileges(@Nonnull List<ApplicationCommandInfo> guildApplicationCommands, @Nonnull Guild guild) {
		for (ApplicationCommandInfo info : guildApplicationCommands) {
			final List<CommandPrivilege> commandPrivileges = new ArrayList<>(10);
			final List<CommandPrivilege> applicationPrivileges = getCommandPrivileges(guild, info);
			if (applicationPrivileges.size() > 10)
				throw new IllegalArgumentException(String.format("There are more than 10 command privileges for command %s in guild %s (%s)", info.getBaseName(), guild.getName(), guild.getId()));

			commandPrivileges.addAll(applicationPrivileges);

			//Add owner-only permissions
			if (ownerOnlyCommands.contains(info.getBaseName())) {
				if (commandPrivileges.size() + context.getOwnerIds().size() > 10)
					throw new IllegalStateException("There should not be more than 10 command privileges (in total) for an owner-only command " + info.getBaseName());

				for (Long ownerId : context.getOwnerIds()) {
					commandPrivileges.add(CommandPrivilege.enableUser(ownerId));
				}
			}

			if (commandPrivileges.isEmpty()) continue;

			cmdBaseNameToPrivilegesMap.put(info.getBaseName(), commandPrivileges);
		}
	}
}