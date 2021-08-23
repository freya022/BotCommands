package com.freya02.botcommands.application;

import com.freya02.botcommands.BContextImpl;
import com.freya02.botcommands.BGuildSettings;
import com.freya02.botcommands.Logging;
import com.freya02.botcommands.SettingsProvider;
import com.freya02.botcommands.application.context.message.MessageCommandInfo;
import com.freya02.botcommands.application.context.user.UserCommandInfo;
import com.freya02.botcommands.application.slash.GuildApplicationSettings;
import com.freya02.botcommands.application.slash.LocalizedApplicationCommandData;
import com.freya02.botcommands.application.slash.SlashCommandInfo;
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
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
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

	private final Map<String, Collection<? extends CommandPrivilege>> cmdIdToPrivilegesMap = new HashMap<>();

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

	//TODO if commands haven't changed but privileges did, we must retrieve the commands and associate them back
	public boolean shouldUpdatePrivileges() throws IOException {
		if (guild == null) return false;

		computePrivileges(guild); //has to run after the commands updated

		if (Files.notExists(privilegesCachePath)) return true;

		final byte[] oldBytes = Files.readAllBytes(privilegesCachePath);
		final byte[] newBytes = ApplicationCommandsCache.getPrivilegesBytes(cmdIdToPrivilegesMap);

		return !Arrays.equals(oldBytes, newBytes);
	}

	public CompletableFuture<?> updatePrivileges() {
		if (guild == null) {
			return CompletableFuture.completedFuture(null);
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

//	private void computeUserCommands(Guild guild, List<ApplicationCommandInfo> guildApplicationCommands) {
//		guildApplicationCommands.stream()
//				.filter(a -> a instanceof UserCommandInfo)
//				.map(a -> (UserCommandInfo) a)
//				.forEachOrdered(info -> {
//					try {
//						final LocalizedSlashCommandData localizedCommandData = getLocalizedCommandData(guild, info, null);
//
//						// User command name
//						final String path = getLocalizedPath(info, localizedCommandData);
//
//						if (info.getPathComponents() == 1) {
//							//Standard command
//							final CommandData rightCommand = new CommandData(CommandType.USER_CONTEXT, path);
//							map.put(path, rightCommand);
//
//							if (info.isOwnerOnly()) {
//								rightCommand.setDefaultEnabled(false);
//							}
//						} else {
//							throw new IllegalStateException("A user command with more than 1 path component got registered");
//						}
//					} catch (Exception e) {
//						throw new RuntimeException("An exception occurred while processing a user command " + info.getPath(), e);
//					}
//				});
//	}
	
	@SuppressWarnings("unchecked")
	private <T extends ApplicationCommandInfo> void computeContextCommands(List<ApplicationCommandInfo> guildApplicationCommands, Class<T> targetClazz, CommandType type) {
		guildApplicationCommands.stream()
				.filter(a -> targetClazz.isAssignableFrom(a.getClass()))
				.map(a -> (T) a)
				.forEachOrdered(info -> {
					try {
						final LocalizedApplicationCommandData localizedCommandData = getLocalizedCommandData(info, null);

						// User command name
						final String path = getLocalizedPath(info, localizedCommandData);

						if (info.getPathComponents() == 1) {
							//Standard command
							final CommandData rightCommand = new CommandData(type, path);
							map.put(path, rightCommand);

							if (info.isOwnerOnly()) {
								rightCommand.setDefaultEnabled(false);
							}
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

	private void computePrivileges(@Nonnull Guild guild) {
		for (Command command : commands) {
			final List<CommandPrivilege> commandPrivileges = new ArrayList<>(10);
			final List<CommandPrivilege> applicationPrivileges = context.getApplicationCommands().stream()
					.filter(a -> a.getName().equals(command.getName()))
					.findFirst()
					.orElseThrow(() -> new IllegalStateException("Could not find any top level command named '" + command.getName() + "'"))
					.getInstance()
					.getCommandPrivileges(guild, command.getName());
			if (applicationPrivileges.size() > 10)
				throw new IllegalArgumentException(String.format("There are more than 10 command privileges for command %s in guild %s (%s)", command.getName(), guild.getName(), guild.getId()));
			
			commandPrivileges.addAll(applicationPrivileges);

			//Add owner-only permissions
			if (ownerOnlyCommands.contains(command.getName())) {
				if (commandPrivileges.size() + context.getOwnerIds().size() > 10)
					throw new IllegalStateException("There should not be more than 10 command privileges (in total) for an owner-only command " + command.getName());

				for (Long ownerId : context.getOwnerIds()) {
					commandPrivileges.add(CommandPrivilege.enableUser(ownerId));
				}
			}

			if (commandPrivileges.isEmpty()) continue;

			cmdIdToPrivilegesMap.put(command.getId(), commandPrivileges);
		}
	}
}