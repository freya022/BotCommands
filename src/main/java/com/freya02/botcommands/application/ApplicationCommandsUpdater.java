package com.freya02.botcommands.application;

import com.freya02.botcommands.BGuildSettings;
import com.freya02.botcommands.SettingsProvider;
import com.freya02.botcommands.application.context.message.MessageCommandInfo;
import com.freya02.botcommands.application.context.user.UserCommandInfo;
import com.freya02.botcommands.application.slash.SlashCommandInfo;
import com.freya02.botcommands.internal.BContextImpl;
import com.freya02.botcommands.internal.Logging;
import com.freya02.botcommands.internal.application.ApplicationCommandMap;
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
import net.dv8tion.jda.internal.utils.Checks;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.freya02.botcommands.application.slash.SlashUtils.*;

public class ApplicationCommandsUpdater {
	private static final Logger LOGGER = Logging.getLogger();

	private final BContextImpl context;
	@Nullable private final Guild guild;

	private final Path commandsCachePath;
	private final Path privilegesCachePath;

	private final ApplicationCommandMap map = new ApplicationCommandMap();

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

		computeCommands();
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
		final byte[] newBytes = ApplicationCommandsCache.getCommandsBytes(map.getAllCommandData());

		return !Arrays.equals(oldBytes, newBytes);
	}

	public CompletableFuture<?> updateCommands() {
		final Collection<CommandData> commandData = map.getAllCommandData();

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

	private void computeCommands() {
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

					return guildSettings.getGuildCommands().getFilter().test(info.getPath().toString());
				})
				.sorted(Comparator.comparingInt(info -> info.getPath().getNameCount()))
				.collect(Collectors.toCollection(ArrayList::new)); //Ensure spliterator is ORDERED for future Stream usage

		computeSlashCommands(guildApplicationCommands);

		computeContextCommands(guildApplicationCommands, UserCommandInfo.class, CommandType.USER_CONTEXT);

		computeContextCommands(guildApplicationCommands, MessageCommandInfo.class, CommandType.MESSAGE_CONTEXT);

		if (guild != null) {
			computePrivileges(guildApplicationCommands, guild);
		}
	}

	private void computeSlashCommands(List<ApplicationCommandInfo> guildApplicationCommands) {
		guildApplicationCommands.stream()
				.filter(a -> a instanceof SlashCommandInfo)
				.map(a -> (SlashCommandInfo) a)
				.forEachOrdered(info -> {
					final CommandPath notLocalizedPath = info.getPath();

					try {
						final List<String> optionNames = getMethodOptionNames(info);
						final LocalizedCommandData localizedCommandData = getLocalizedCommandData(info, optionNames);

						//Put localized option names in order to resolve them when called
						final List<OptionData> localizedMethodOptions = getMethodOptions(info, localizedCommandData);
						if (guild != null) {
							info.putLocalizedOptions(guild.getIdLong(), localizedMethodOptions.stream().map(OptionData::getName).collect(Collectors.toList()));
						}

						localizedBaseNameToBaseName.put(getLocalizedPath(info, localizedCommandData).getName(), notLocalizedPath.getName());

						final CommandPath localizedPath = getLocalizedPath(info, localizedCommandData);
						final String description = getLocalizedDescription(info, localizedCommandData);

						Checks.check(localizedPath.getNameCount() == notLocalizedPath.getNameCount(), "Localized path does not have the same name count as the not-localized path");

						if (localizedPath.getNameCount() == 1) {
							//Standard command
							final CommandData rightCommand = new CommandData(localizedPath.getName(), description);
							map.put(CommandType.SLASH, localizedPath, rightCommand);

							rightCommand.addOptions(localizedMethodOptions);

							if (info.isOwnerOnly()) {
								rightCommand.setDefaultEnabled(false);
							}
						} else if (localizedPath.getNameCount() == 2) {
							Checks.notNull(localizedPath.getSubname(), "Subcommand name");

							final CommandData commandData = map.computeIfAbsent(CommandType.SLASH, localizedPath, x -> {
								final CommandData tmpData = new CommandData(localizedPath.getName(), "No description (base name)");
								if (info.isOwnerOnly()) {
									tmpData.setDefaultEnabled(false);
								}

								return tmpData;
							});

							//Subcommand of a command
							final SubcommandData subcommandData = new SubcommandData(localizedPath.getSubname(), description);
							commandData.addSubcommands(subcommandData);

							subcommandData.addOptions(localizedMethodOptions);
						} else if (localizedPath.getNameCount() == 3) {
							Checks.notNull(localizedPath.getGroup(), "Command group name");
							Checks.notNull(localizedPath.getSubname(), "Subcommand name");

							final SubcommandGroupData groupData = getSubcommandGroup(CommandType.SLASH, localizedPath, x -> {
								final CommandData commandData = new CommandData(localizedPath.getName(), "No description (base name)");

								if (info.isOwnerOnly()) {
									commandData.setDefaultEnabled(false);
								}

								return commandData;
							});

							final SubcommandData subcommandData = new SubcommandData(localizedPath.getSubname(), description);
							groupData.addSubcommands(subcommandData);

							subcommandData.addOptions(localizedMethodOptions);
						} else {
							throw new IllegalStateException("A slash command with more than 4 path components got registered");
						}

						context.addSlashCommandAlternative(localizedPath, info);

						if (!info.isOwnerOnly()) {
							if (ownerOnlyCommands.contains(notLocalizedPath.getName())) {
								LOGGER.warn("Non owner-only command '{}' is registered as a owner-only command because of another command with the same base name '{}'", notLocalizedPath, notLocalizedPath.getName());
							}
						}

						if (info.isOwnerOnly()) {
							if (info.isGuildOnly()) {
								ownerOnlyCommands.add(notLocalizedPath.getName());
							} else {
								LOGGER.warn("Owner-only command '{}' cannot be owner-only as it is a global command", notLocalizedPath);
							}
						}
					} catch (Exception e) {
						throw new RuntimeException("An exception occurred while processing command " + notLocalizedPath, e);
					}
				});
	}

	@SuppressWarnings("unchecked")
	private <T extends ApplicationCommandInfo> void computeContextCommands(List<ApplicationCommandInfo> guildApplicationCommands, Class<T> targetClazz, CommandType type) {
		guildApplicationCommands.stream()
				.filter(a -> targetClazz.isAssignableFrom(a.getClass()))
				.map(a -> (T) a)
				.forEachOrdered(info -> {
					final CommandPath notLocalizedPath = info.getPath();

					try {
						final LocalizedCommandData localizedCommandData = getLocalizedCommandData(info, null);

						localizedBaseNameToBaseName.put(getLocalizedPath(info, localizedCommandData).getName(), notLocalizedPath.getName());

						// User command name
						final CommandPath localizedPath = getLocalizedPath(info, localizedCommandData);

						Checks.check(localizedPath.getNameCount() == notLocalizedPath.getNameCount(), "Localized path does not have the same name count as the not-localized path");

						if (localizedPath.getNameCount() == 1) {
							//Standard command
							final CommandData rightCommand = new CommandData(type, localizedPath.getName());
							map.put(type, localizedPath, rightCommand);

							if (info.isOwnerOnly()) {
								rightCommand.setDefaultEnabled(false);

								ownerOnlyCommands.add(notLocalizedPath.getName()); //Must be non-localized name
							}
						} else {
							throw new IllegalStateException("A " + type.name() + " command with more than 1 path component got registered");
						}
					} catch (Exception e) {
						throw new RuntimeException("An exception occurred while processing a " + type.name() + " command " + notLocalizedPath, e);
					}
				});
	}

	@Nullable
	private LocalizedCommandData getLocalizedCommandData(ApplicationCommandInfo info, @Nullable List<String> optionNames) {
		final GuildApplicationSettings instance = info.getInstance();
		final LocalizedCommandData localizedCommandData = instance.getLocalizedCommandData(guild, info.getPath().getFullPath(), optionNames);

		if (localizedCommandData == null) {
			final SettingsProvider settingsProvider = context.getSettingsProvider();

			if (settingsProvider != null) {
				return settingsProvider.getLocalizedCommandData(guild, info.getPath().getFullPath(), optionNames);
			}
		}

		return localizedCommandData;
	}

	@Nonnull
	private List<CommandPrivilege> getCommandPrivileges(@Nonnull Guild guild, @Nonnull ApplicationCommandInfo info) {
		final List<CommandPrivilege> instancePrivileges = info.getInstance().getCommandPrivileges(guild, info.getPath().getName());
		if (!instancePrivileges.isEmpty()) {
			return instancePrivileges;
		} else {
			final SettingsProvider settingsProvider = context.getSettingsProvider();

			if (settingsProvider != null) {
				return settingsProvider.getCommandPrivileges(guild, info.getPath().getName());
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
				throw new IllegalArgumentException(String.format("There are more than 10 command privileges for command %s in guild %s (%s)", info.getPath().getName(), guild.getName(), guild.getId()));

			commandPrivileges.addAll(applicationPrivileges);

			//Add owner-only permissions
			if (ownerOnlyCommands.contains(info.getPath().getName())) {
				if (commandPrivileges.size() + context.getOwnerIds().size() > 10)
					throw new IllegalStateException("There should not be more than 10 command privileges (in total) for an owner-only command " + info.getPath().getName());

				for (Long ownerId : context.getOwnerIds()) {
					commandPrivileges.add(CommandPrivilege.enableUser(ownerId));
				}
			}

			if (commandPrivileges.isEmpty()) continue;

			cmdBaseNameToPrivilegesMap.put(info.getPath().getName(), commandPrivileges);
		}
	}

	//I am aware that the type is always CommandType#SLASH, still use a parameter to mimic how ApplicationCommandMap functions and for future proof uses
	@SuppressWarnings("SameParameterValue")
	@Nonnull
	private SubcommandGroupData getSubcommandGroup(CommandType type, CommandPath path, Function<String, CommandData> baseCommandSupplier) {
		if (path.getGroup() == null)
			throw new IllegalArgumentException("Group component of command path is null at '" + path + "'");

		final CommandData data = map.computeIfAbsent(type, path, baseCommandSupplier);

		return data.getSubcommandGroups()
				.stream()
				.filter(g -> g.getName().equals(path.getGroup()))
				.findAny()
				.orElseGet(() -> {
					final SubcommandGroupData groupData = new SubcommandGroupData(path.getGroup(), "No description (group)");

					data.addSubcommandGroups(groupData);

					return groupData;
				});
	}
}