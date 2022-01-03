package com.freya02.botcommands.internal.application;

import com.freya02.botcommands.api.Logging;
import com.freya02.botcommands.api.SettingsProvider;
import com.freya02.botcommands.api.application.CommandPath;
import com.freya02.botcommands.internal.BContextImpl;
import com.freya02.botcommands.internal.application.context.message.MessageCommandInfo;
import com.freya02.botcommands.internal.application.context.user.UserCommandInfo;
import com.freya02.botcommands.internal.application.slash.SlashCommandInfo;
import com.freya02.botcommands.internal.utils.Utils;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.*;
import net.dv8tion.jda.api.interactions.commands.privileges.CommandPrivilege;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.dv8tion.jda.internal.utils.Checks;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.freya02.botcommands.internal.application.slash.SlashUtils.appendCommands;
import static com.freya02.botcommands.internal.application.slash.SlashUtils.getLocalizedMethodOptions;

public class ApplicationCommandsUpdater {
	private static final Logger LOGGER = Logging.getLogger();

	private final BContextImpl context;
	@Nullable private final Guild guild;

	private final Path commandsCachePath;
	private final Path privilegesCachePath;

	private final ApplicationCommandDataMap map = new ApplicationCommandDataMap();

	private final List<String> ownerOnlyCommands = new ArrayList<>();
	private final List<Command> commands = new ArrayList<>();

	private final Map<String, String> localizedBaseNameToBaseName = new HashMap<>();
	private final Map<String, Collection<? extends CommandPrivilege>> cmdIdToPrivilegesMap = new HashMap<>();
	private final Map<String, Collection<? extends CommandPrivilege>> cmdBaseNameToPrivilegesMap = new HashMap<>();
	private final Collection<CommandData> allCommandData;

	private ApplicationCommandsUpdater(@NotNull BContextImpl context, @Nullable Guild guild) throws IOException {
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

		this.allCommandData = map.getAllCommandData();
	}

	public static ApplicationCommandsUpdater ofGlobal(@NotNull BContextImpl context) throws IOException {
		return new ApplicationCommandsUpdater(context, null);
	}

	public static ApplicationCommandsUpdater ofGuild(@NotNull BContextImpl context, @NotNull Guild guild) throws IOException {
		return new ApplicationCommandsUpdater(context, guild);
	}

	@Nullable
	public Guild getGuild() {
		return guild;
	}

	@Blocking
	public boolean shouldUpdateCommands() throws IOException {
		final byte[] oldBytes;

		if (context.isOnlineAppCommandCheckEnabled()) {
			//TODO Discord sends default_permissions as always true when CommandData.default_permissions is set to false

			final List<Command> discordCommands = (guild == null ? context.getJDA().retrieveCommands() : guild.retrieveCommands()).complete();
			final List<CommandData> discordCommandsData = discordCommands.stream().map(CommandData::fromCommand).toList();

			oldBytes = ApplicationCommandsCache.getCommandsBytes(discordCommandsData);
		} else {
			if (Files.notExists(commandsCachePath)) {
				LOGGER.trace("Updating commands because cache file does not exists");

				return true;
			}

			oldBytes = Files.readAllBytes(commandsCachePath);
		}

		final byte[] newBytes = ApplicationCommandsCache.getCommandsBytes(allCommandData);

		final boolean needUpdate = !ApplicationCommandsCache.isJsonContentSame(oldBytes, newBytes);

		if (needUpdate) {
			LOGGER.trace("Updating commands because content is not equal");

//			LOGGER.trace("Old commands bytes: {}", new String(oldBytes));
//			LOGGER.trace("New commands bytes: {}", new String(newBytes));
		}

		return needUpdate;
	}

	@Blocking
	public void updateCommands() {
		final CommandListUpdateAction updateAction = guild != null ? guild.updateCommands() : context.getJDA().updateCommands();

		final List<Command> commands = updateAction
				.addCommands(allCommandData)
				.complete();

		if (guild != null) {
			thenAcceptGuild(commands, guild);
		} else {
			thenAcceptGlobal(commands);
		}
	}

	public boolean shouldUpdatePrivileges() throws IOException {
		if (guild == null) return false;

		if (!commands.isEmpty()) return true; //If the list is not empty, this means commands got updated, so the ids changed

		final byte[] oldBytes;
//		if (!context.isOnlineAppCommandCheckEnabled()) {
			if (Files.notExists(privilegesCachePath)) {
				LOGGER.trace("Updating privileges because privilege cache does not exists");

				return true;
			}

			oldBytes = Files.readAllBytes(privilegesCachePath);
//		} else {
//			final Map<String, List<CommandPrivilege>> privilegesMap = guild.retrieveCommandPrivileges().complete();
//
//			TODO wait for discord localisation so i can remove these base name mappings
//		}

		final byte[] newBytes = ApplicationCommandsCache.getPrivilegesBytes(cmdBaseNameToPrivilegesMap);

		final boolean needUpdate = !ApplicationCommandsCache.isJsonContentSame(oldBytes, newBytes);

		if (needUpdate) {
			LOGGER.trace("Updating privileges because content is not equal");

//			LOGGER.trace("Old privileges bytes: {}", new String(oldBytes));
//			LOGGER.trace("New privileges bytes: {}", new String(newBytes));
		}

		return needUpdate;
	}

	@Blocking
	public void updatePrivileges() {
		if (guild == null) {
			return;
		}

		if (commands.isEmpty()) {
			LOGGER.info("Privileges has changed but commands were not updated, retrieving current command list");

			final List<Command> retrievedCommands = guild.retrieveCommands().complete();

			updatePrivileges0(guild, retrievedCommands);
		} else {
			updatePrivileges0(guild, commands);
		}
	}

	@Blocking
	private void updatePrivileges0(@NotNull Guild guild, @NotNull List<Command> commands) {
		for (Command command : commands) {
			final Collection<? extends CommandPrivilege> privileges = cmdBaseNameToPrivilegesMap.get(localizedBaseNameToBaseName.get(command.getName()));

			if (privileges != null) {
				cmdIdToPrivilegesMap.put(command.getId(), privileges);
			}
		}

		guild.updateCommandPrivileges(cmdIdToPrivilegesMap).complete();

		try {
			Files.write(privilegesCachePath, ApplicationCommandsCache.getPrivilegesBytes(cmdBaseNameToPrivilegesMap), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			LOGGER.error("An exception occurred while temporarily saving guild ({} ({})) command privileges", guild.getName(), guild.getId(), e);
		}
	}

	private void computeCommands() {
		final List<ApplicationCommandInfo> guildApplicationCommands = context.getApplicationCommandInfoMap().filterByGuild(context, guild);

		computeSlashCommands(guildApplicationCommands);

		computeContextCommands(guildApplicationCommands, UserCommandInfo.class, Command.Type.USER);

		computeContextCommands(guildApplicationCommands, MessageCommandInfo.class, Command.Type.MESSAGE);

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
						final LocalizedCommandData localizedCommandData = LocalizedCommandData.of(context, guild, info);

						//Put localized option names in order to resolve them when called
						final List<OptionData> localizedMethodOptions = getLocalizedMethodOptions(info, localizedCommandData);
						if (guild != null) {
							info.putLocalizedOptions(guild.getIdLong(), localizedMethodOptions.stream().map(OptionData::getName).collect(Collectors.toList()));
						}

						localizedBaseNameToBaseName.put(localizedCommandData.getLocalizedPath().getName(), notLocalizedPath.getName());

						final CommandPath localizedPath = localizedCommandData.getLocalizedPath();
						final String description = localizedCommandData.getLocalizedDescription();

						Checks.check(localizedPath.getNameCount() == notLocalizedPath.getNameCount(), "Localized path does not have the same name count as the not-localized path");

						if (localizedPath.getNameCount() == 1) {
							//Standard command
							final SlashCommandData rightCommand = Commands.slash(localizedPath.getName(), description);
							map.put(Command.Type.SLASH, localizedPath, rightCommand);

							rightCommand.addOptions(localizedMethodOptions);

							if (info.isOwnerRequired()) {
								rightCommand.setDefaultEnabled(false);
							}
						} else if (localizedPath.getNameCount() == 2) {
							Checks.notNull(localizedPath.getSubname(), "Subcommand name");

							final SlashCommandData commandData = (SlashCommandData) map.computeIfAbsent(Command.Type.SLASH, localizedPath, x -> {
								final SlashCommandData tmpData = Commands.slash(localizedPath.getName(), "No description (base name)");
								if (info.isOwnerRequired()) {
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

							final SubcommandGroupData groupData = getSubcommandGroup(Command.Type.SLASH, localizedPath, x -> {
								final SlashCommandData commandData = Commands.slash(localizedPath.getName(), "No description (base name)");

								if (info.isOwnerRequired()) {
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

						context.addApplicationCommandAlternative(localizedPath, Command.Type.SLASH, info);

						if (!info.isOwnerRequired()) {
							if (ownerOnlyCommands.contains(notLocalizedPath.getName())) {
								LOGGER.warn("Non owner-only command '{}' is registered as a owner-only command because of another command with the same base name '{}'", notLocalizedPath, notLocalizedPath.getName());
							}
						}

						if (info.isOwnerRequired()) {
							if (info.isGuildOnly()) {
								ownerOnlyCommands.add(notLocalizedPath.getName());
							} else {
								LOGGER.warn("Owner-only command '{}' cannot be owner-only as it is a global command", notLocalizedPath);
							}
						}
					} catch (Exception e) {
						throw new RuntimeException("An exception occurred while processing command '" + notLocalizedPath + "' at " + Utils.formatMethodShort(info.getCommandMethod()), e);
					}
				});
	}

	@SuppressWarnings("unchecked")
	private <T extends ApplicationCommandInfo> void computeContextCommands(List<ApplicationCommandInfo> guildApplicationCommands, Class<T> targetClazz, Command.Type type) {
		guildApplicationCommands.stream()
				.filter(a -> targetClazz.isAssignableFrom(a.getClass()))
				.map(a -> (T) a)
				.forEachOrdered(info -> {
					final CommandPath notLocalizedPath = info.getPath();

					try {
						final LocalizedCommandData localizedCommandData = LocalizedCommandData.of(context, guild, info);

						localizedBaseNameToBaseName.put(localizedCommandData.getLocalizedPath().getName(), notLocalizedPath.getName());

						// User command name
						final CommandPath localizedPath = localizedCommandData.getLocalizedPath();

						Checks.check(localizedPath.getNameCount() == notLocalizedPath.getNameCount(), "Localized path does not have the same name count as the not-localized path");

						if (localizedPath.getNameCount() == 1) {
							//Standard command
							final CommandData rightCommand = Commands.context(type, localizedPath.getName());
							map.put(type, localizedPath, rightCommand);

							if (info.isOwnerRequired()) {
								rightCommand.setDefaultEnabled(false);

								ownerOnlyCommands.add(notLocalizedPath.getName()); //Must be non-localized name
							}
						} else {
							throw new IllegalStateException("A " + type.name() + " command with more than 1 path component got registered");
						}

						context.addApplicationCommandAlternative(localizedPath, type, info);
					} catch (Exception e) {
						throw new RuntimeException("An exception occurred while processing a " + type.name() + " command " + notLocalizedPath, e);
					}
				});
	}

	@NotNull
	private List<CommandPrivilege> getCommandPrivileges(@NotNull Guild guild, @NotNull ApplicationCommandInfo info) {
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

	private void thenAcceptGuild(List<Command> commands, @NotNull Guild guild) {
		for (Command command : commands) {
			context.getRegistrationListeners().forEach(l -> l.onGuildSlashCommandRegistered(this.guild, command));
		}

		this.commands.addAll(commands);

		try {
			Files.write(commandsCachePath, ApplicationCommandsCache.getCommandsBytes(allCommandData), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			LOGGER.error("An exception occurred while temporarily saving guild ({} ({})) commands in '{}'", guild.getName(), guild.getId(), commandsCachePath.toAbsolutePath(), e);
		}

		if (!LOGGER.isTraceEnabled()) return;

		final StringBuilder sb = new StringBuilder("Updated " + commands.size() + " / " + allCommandData.size() + " (" + context.getApplicationCommandsView().size() + ") commands for ");
		sb.append(guild.getName()).append(" :\n");
		appendCommands(commands, sb);

		LOGGER.trace(sb.toString().trim());
	}

	private void thenAcceptGlobal(List<Command> commands) {
		for (Command command : commands) {
			context.getRegistrationListeners().forEach(l -> l.onGlobalSlashCommandRegistered(command));
		}

		try {
			Files.write(commandsCachePath, ApplicationCommandsCache.getCommandsBytes(allCommandData), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			LOGGER.error("An exception occurred while temporarily saving {} commands in '{}'", guild == null ? "global" : String.format("guild '%s' (%s)", guild.getName(), guild.getId()), commandsCachePath.toAbsolutePath(), e);
		}

		if (!LOGGER.isTraceEnabled()) return;

		final StringBuilder sb = new StringBuilder("Updated global commands:\n");
		appendCommands(commands, sb);

		LOGGER.trace(sb.toString().trim());
	}

	//See how to integrate this step on the command build in order to use localized base names instead of resolving the not-localized one
	//Could do it here but would result in a 2nd localized data call
	private void computePrivileges(@NotNull List<ApplicationCommandInfo> guildApplicationCommands, @NotNull Guild guild) {
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

	//I am aware that the type is always Command.Type#SLASH, still use a parameter to mimic how ApplicationCommandMap functions and for future proof uses
	@SuppressWarnings("SameParameterValue")
	@NotNull
	private SubcommandGroupData getSubcommandGroup(Command.Type type, CommandPath path, Function<String, CommandData> baseCommandSupplier) {
		if (path.getGroup() == null)
			throw new IllegalArgumentException("Group component of command path is null at '" + path + "'");

		final SlashCommandData data = (SlashCommandData) map.computeIfAbsent(type, path, baseCommandSupplier);

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