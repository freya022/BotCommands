package com.freya02.botcommands.slash;

import com.freya02.botcommands.BContextImpl;
import com.freya02.botcommands.Logging;
import com.freya02.botcommands.SettingsProvider;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import net.dv8tion.jda.api.interactions.commands.privileges.CommandPrivilege;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static com.freya02.botcommands.slash.SlashUtils.*;

public class SlashCommandsUpdater {
	private static final Logger LOGGER = Logging.getLogger();

	private final BContextImpl context;
	private final Guild guild;

	private final Path commandsCachePath;
	private final Path privilegesCachePath;

	private final Map<String, CommandData> map = new HashMap<>();
	private final Map<String, SubcommandGroupData> groupMap = new HashMap<>();

	private final List<String> ownerOnlyCommands = new ArrayList<>();
	private final List<Command> commands = new ArrayList<>();

	private final Map<String, Collection<? extends CommandPrivilege>> cmdIdToPrivilegesMap = new HashMap<>();

	private SlashCommandsUpdater(@NotNull BContextImpl context, @Nullable Guild guild) throws IOException {
		this.context = context;
		this.guild = guild;

		this.commandsCachePath = guild == null
				? context.getSlashCommandsCache().getGlobalCommandsPath()
				: context.getSlashCommandsCache().getGuildCommandsPath(guild);

		Files.createDirectories(commandsCachePath.getParent());

		if (guild == null) {
			this.privilegesCachePath = null;
		} else {
			this.privilegesCachePath = context.getSlashCommandsCache().getGuildPrivilegesPath(guild);

			Files.createDirectories(privilegesCachePath.getParent());
		}

		computeCommands(context, guild);
	}

	public static SlashCommandsUpdater ofGlobal(@NotNull BContextImpl context) throws IOException {
		return new SlashCommandsUpdater(context, null);
	}

	public static SlashCommandsUpdater ofGuild(@NotNull BContextImpl context, @NotNull Guild guild) throws IOException {
		return new SlashCommandsUpdater(context, guild);
	}

	public Guild getGuild() {
		return guild;
	}

	public boolean shouldUpdateCommands() throws IOException {
		if (Files.notExists(commandsCachePath)) return true;

		final byte[] oldBytes = Files.readAllBytes(commandsCachePath);
		final byte[] newBytes = SlashCommandsCache.getCommandsBytes(map.values());

		return !Arrays.equals(oldBytes, newBytes);
	}

	public CompletableFuture<?> updateCommands() {
		final Collection<CommandData> commandData = map.values();

		final CommandListUpdateAction updateAction = guild != null ? guild.updateCommands() : context.getJDA().updateCommands();

		final CompletableFuture<List<Command>> future = updateAction
				.addCommands(commandData)
				.submit();

		return guild != null ? thenAcceptGuild(commandData, future) : thenAcceptGlobal(commandData, future);
	}

	//TODO if commands haven't changed but privileges did, we must retrieve the commands and associate them back
	public boolean shouldUpdatePrivileges() throws IOException {
		if (guild == null) return false;

		computePrivileges(); //has to run after the commands updated

		if (Files.notExists(privilegesCachePath)) return true;

		final byte[] oldBytes = Files.readAllBytes(privilegesCachePath);
		final byte[] newBytes = SlashCommandsCache.getPrivilegesBytes(cmdIdToPrivilegesMap);

		return !Arrays.equals(oldBytes, newBytes);
	}

	public CompletableFuture<?> updatePrivileges() {
		if (guild == null) {
			return CompletableFuture.completedFuture(null);
		}

		return guild.updateCommandPrivileges(cmdIdToPrivilegesMap).submit().thenAccept(privilegesMap -> {
			try {
				Files.write(privilegesCachePath, SlashCommandsCache.getPrivilegesBytes(cmdIdToPrivilegesMap), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
			} catch (IOException e) {
				LOGGER.error("An exception occurred while temporarily saving guild ({} ({})) command privileges", guild.getName(), guild.getId(), e);
			}
		});
	}

	private void computeCommands(@NotNull BContextImpl context, @Nullable Guild guild) {
		context.getSlashCommands().stream()
				.filter(info -> {
					if (info.isGuildOnly() && guild == null) { //Do not update guild-only commands in global context
						return false;
					} else if (!info.isGuildOnly() && guild != null) { //Do not update global commands in guild context
						return false;
					}

					//Get the actual usable commands in this context (dm or guild)
					if (guild == null) return true;

					return context.getPermissionProvider().getGuildCommands(guild).getFilter().test(info.getPath());
				})
				.sorted(Comparator.comparingInt(SlashCommandInfo::getPathComponents))
				.forEachOrdered(info -> {
					try {
						final List<String> optionNames = getMethodOptionNames(info);
						final LocalizedSlashCommandData localizedCommandData = getLocalizedCommandData(guild, info, optionNames);

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
							throw new IllegalStateException("A slash command with more than 4 names got registered");
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

	@Nullable
	private LocalizedSlashCommandData getLocalizedCommandData(@Nullable Guild guild, SlashCommandInfo info, List<String> optionNames) {
		final LocalizedSlashCommandData localizedCommandData = info.getInstance().getLocalizedCommandData(guild, info.getPath(), optionNames);
		
		if (localizedCommandData == null) {
			final SettingsProvider settingsProvider = context.getSettingsProvider();
			
			if (settingsProvider != null) {
				return settingsProvider.getLocalizedCommandData(guild, info.getPath(), optionNames);
			}
		}
		
		return localizedCommandData;
	}

	private CompletableFuture<?> thenAcceptGuild(Collection<CommandData> commandData, CompletableFuture<List<Command>> future) {
		return future.thenAccept(commands -> {
			for (Command command : commands) {
				context.getRegistrationListeners().forEach(l -> l.onGuildSlashCommandRegistered(guild, command));
			}

			this.commands.addAll(commands);

			try {
				Files.write(commandsCachePath, SlashCommandsCache.getCommandsBytes(commandData), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
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
				context.getRegistrationListeners().forEach(l -> l.onGlobalSlashCommandRegistered(command));
			}

			try {
				Files.write(commandsCachePath, SlashCommandsCache.getCommandsBytes(commandData), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
			} catch (IOException e) {
				LOGGER.error("An exception occurred while temporarily saving {} commands in '{}'", guild == null ? "global" : String.format("guild '%s' (%s)", guild.getName(), guild.getId()), commandsCachePath.toAbsolutePath(), e);
			}

			if (!LOGGER.isTraceEnabled()) return;

			final StringBuilder sb = new StringBuilder("Updated global commands:\n");
			appendCommands(commands, sb);

			LOGGER.trace(sb.toString().trim());
		});
	}

	private void computePrivileges() {
		for (Command command : commands) {
			final List<CommandPrivilege> commandPrivileges = new ArrayList<>(context.getPermissionProvider().getPermissions(command.getName(), guild));
			if (commandPrivileges.size() > 10)
				throw new IllegalArgumentException(String.format("There are more than 10 command privileges for command %s in guild %s (%s)", command.getName(), guild.getName(), guild.getId()));

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