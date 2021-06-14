package com.freya02.botcommands.slash;

import com.freya02.botcommands.BContextImpl;
import com.freya02.botcommands.Logging;
import com.freya02.botcommands.PermissionProvider;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import net.dv8tion.jda.api.interactions.commands.privileges.CommandPrivilege;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.freya02.botcommands.slash.SlashCommandsBuilder.*;

public class CachedSlashCommands {
	private static final Logger LOGGER = Logging.getLogger();

	private final BContextImpl context;

	private final Map<String, CommandData> guildMap = new HashMap<>();
	private final Map<String, CommandData> globalMap = new HashMap<>();

	private final List<String> ownerOnlyCommands = new ArrayList<>();

	private final Map<Long, Collection<CommandData>> guildToCommandsDataMap = Collections.synchronizedMap(new HashMap<>());
	private final Map<Long, List<Command>> guildToCommandsMap = Collections.synchronizedMap(new HashMap<>());
	private final Map<Long, Map<String, Collection<? extends CommandPrivilege>>> guildToPrivilegesMap = Collections.synchronizedMap(new HashMap<>());

	public CachedSlashCommands(BContextImpl context) {
		this.context = context;
	}

	private static Path getTempPath(String filename) {
		return Path.of(System.getProperty("java.io.tmpdir") + filename);
	}

	public void computeCommands() {
		final Map<String, SubcommandGroupData> guildGroupMap = new HashMap<>();
		final Map<String, SubcommandGroupData> globalGroupMap = new HashMap<>();

		context.getSlashCommands().stream().sorted(Comparator.comparingInt(SlashCommandInfo::getPathComponents)).forEachOrdered(info -> {
			try {
				final Map<String, CommandData> map = info.isGuildOnly() ? guildMap : globalMap;
				final Map<String, SubcommandGroupData> groupMap = info.isGuildOnly() ? guildGroupMap : globalGroupMap;

				final String path = info.getPath();
				if (info.getPathComponents() == 1) {
					//Standard command
					final CommandData rightCommand = new CommandData(info.getName(), info.getDescription());
					map.put(path, rightCommand);

					rightCommand.addOptions(getMethodOptions(info));

					if (info.isOwnerOnly()) {
						rightCommand.setDefaultEnabled(false);
					}
				} else if (info.getPathComponents() == 2) {
					//Subcommand of a command
					final String parent = getParent(path);
					final CommandData commandData = map.computeIfAbsent(parent, s -> {
						final CommandData tmpData = new CommandData(getName(parent), "we can't see this rite ?");
						if (info.isOwnerOnly()) {
							tmpData.setDefaultEnabled(false);
						}

						return tmpData;
					});

					final SubcommandData rightCommand = new SubcommandData(info.getName(), info.getDescription());
					commandData.addSubcommands(rightCommand);

					rightCommand.addOptions(getMethodOptions(info));
				} else if (info.getPathComponents() == 3) {
					final String namePath = getParent(getParent(path));
					final String parentPath = getParent(path);
					final SubcommandGroupData groupData = groupMap.computeIfAbsent(parentPath, gp -> {
						final CommandData nameData = new CommandData(getName(namePath), "we can't see r-right ?");
						map.put(getName(namePath), nameData);

						if (info.isOwnerOnly()) {
							nameData.setDefaultEnabled(false);
						}

						final SubcommandGroupData groupDataTmp = new SubcommandGroupData(getName(parentPath), "we can't see r-right ?");
						nameData.addSubcommandGroups(groupDataTmp);

						return groupDataTmp;
					});

					final SubcommandData rightCommand = new SubcommandData(info.getName(), info.getDescription());
					groupData.addSubcommands(rightCommand);

					rightCommand.addOptions(getMethodOptions(info));
				} else {
					throw new IllegalStateException("A slash command with more than 4 names got registered");
				}

				if (!info.isOwnerOnly()) {
					if (ownerOnlyCommands.contains(info.getBaseName())) {
						LOGGER.warn("Non owner-only command '{}' is registered as a owner-only command because of another command with the same base name '{}'", info.getPath(), info.getBaseName());
					}
				}

				if (info.isOwnerOnly() && info.isGuildOnly()) {
					ownerOnlyCommands.add(info.getBaseName());
				}
			} catch (Exception e) {
				throw new RuntimeException("An exception occurred while processing command " + info.getPath(), e);
			}
		});
	}

	public boolean shouldUpdateGlobalCommands() throws IOException {
		final Path path = getTempPath("globalCommands.json");

		if (Files.notExists(path)) return true;

		final byte[] jsonBytes = getGlobalCommandsBytes();
		final byte[] oldBytes = Files.readAllBytes(path);

		return !Arrays.equals(jsonBytes, oldBytes);
	}

	@Nonnull
	private byte[] getGlobalCommandsBytes() {
		DataArray json = DataArray.empty();
		json.addAll(globalMap.values());

		return json.toJson();
	}

	public void updateGlobalCommands() {
		context.getJDA().updateCommands()
				.addCommands(globalMap.values())
				.queue(commands -> {
					for (Command command : commands) {
						context.getRegistrationListeners().forEach(l -> l.onGlobalSlashCommandRegistered(command));
					}

					final Path path = getTempPath("globalCommands.json");
					try {
						Files.write(path, getGlobalCommandsBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
					} catch (IOException e) {
						if (LOGGER.isErrorEnabled()) {
							LOGGER.error("An exception occurred while temporarily saving global commands in '{}'", path.toAbsolutePath(), e);
						} else {
							System.err.printf("An exception occurred while temporarily saving global commands in '%s'%n", path.toAbsolutePath());
							e.printStackTrace();
						}
					}

					if (!LOGGER.isTraceEnabled()) return;

					final StringBuilder sb = new StringBuilder("Updated global commands:\n");
					appendCommands(commands, sb);

					LOGGER.trace(sb.toString().trim());
				});
	}

	public void computeGuildCommands(Guild guild) {
		final PermissionProvider permissionProvider = context.getPermissionProvider();
		final Collection<String> commandNames = permissionProvider.getGuildCommands(guild.getId());

		final Collection<CommandData> commandData;
		if (commandNames.isEmpty()) {
			commandData = guildMap.values();
		} else {
			commandData = guildMap
					.values()
					.stream()
					.filter(c -> commandNames.contains(c.getName()))
					.collect(Collectors.toList());
		}

		guildToCommandsDataMap.put(guild.getIdLong(), commandData);
	}

	public boolean shouldUpdateGuildCommands(Guild guild) throws IOException {
		final Path path = getTempPath(guild.getId() + "Commands.json");

		if (Files.notExists(path)) return true;

		final byte[] jsonBytes = getGuildCommandsBytes(guild);
		final byte[] oldBytes = Files.readAllBytes(path);

		return !Arrays.equals(jsonBytes, oldBytes);
	}

	private byte[] getGuildCommandsBytes(Guild guild) {
		DataArray json = DataArray.empty();
		json.addAll(guildToCommandsDataMap.get(guild.getIdLong()));

		return json.toJson();
	}

	public CompletableFuture<?> updateGuildCommands(Guild guild) {
		final Collection<CommandData> commandData = guildToCommandsDataMap.get(guild.getIdLong());

		return guild.updateCommands()
				.addCommands(commandData)
				.submit()
				.thenAccept(commands -> {
					for (Command command : commands) {
						context.getRegistrationListeners().forEach(l -> l.onGuildSlashCommandRegistered(guild, command));
					}

					guildToCommandsMap.put(guild.getIdLong(), commands);

					final Path path = getTempPath(guild.getId() + "Commands.json");
					try {
						Files.write(path, getGuildCommandsBytes(guild), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
					} catch (IOException e) {
						if (LOGGER.isErrorEnabled()) {
							LOGGER.error("An exception occurred while temporarily saving guild ({} ({})) commands in '{}'", guild.getName(), guild.getId(), path.toAbsolutePath(), e);
						} else {
							System.err.printf("An exception occurred while temporarily saving guild (%s (%d)) commands in '%s'%n", guild.getName(), guild.getIdLong(), path.toAbsolutePath());
							e.printStackTrace();
						}
					}

					if (!LOGGER.isTraceEnabled()) return;

					final StringBuilder sb = new StringBuilder("Updated commands for ");
					sb.append(guild.getName()).append(" :\n");
					appendCommands(commands, sb);

					LOGGER.trace(sb.toString().trim());
				});
	}

	public void computeGuildPrivileges(Guild guild) {
		Map<String, Collection<? extends CommandPrivilege>> privileges = new HashMap<>();
		for (Command command : getGuildCommands(guild)) {
			final List<CommandPrivilege> commandPrivileges = new ArrayList<>(context.getPermissionProvider().getPermissions(command.getName(), guild.getId()));
			if (commandPrivileges.size() > 10)
				throw new IllegalArgumentException(String.format("There are more than 10 command privileges for command %s in guild %s (%s)", command.getName(), guild.getName(), guild.getId()));

			if (ownerOnlyCommands.contains(command.getName())) {
				if (commandPrivileges.size() + context.getOwnerIds().size() > 10)
					throw new IllegalStateException("There should not be more than 10 command privileges (in total) for an owner-only command " + command.getName());

				for (Long ownerId : context.getOwnerIds()) {
					commandPrivileges.add(CommandPrivilege.enableUser(ownerId));
				}
			}

			if (commandPrivileges.isEmpty()) continue;

			privileges.put(command.getId(), commandPrivileges);
		}

		guildToPrivilegesMap.put(guild.getIdLong(), privileges);
	}

	public boolean shouldUpdateGuildPrivileges(Guild guild) throws IOException {
		final Path path = getTempPath(guild.getId() + "Privileges.json");

		if (Files.notExists(path)) return true;

		final byte[] jsonBytes = getGuildCommandPrivilegesBytes(guild);
		final byte[] oldBytes = Files.readAllBytes(path);

		return !Arrays.equals(jsonBytes, oldBytes);
	}

	private byte[] getGuildCommandPrivilegesBytes(Guild guild) {
		final Map<String, Collection<? extends CommandPrivilege>> privileges = guildToPrivilegesMap.get(guild.getIdLong());

		DataArray array = DataArray.empty();
		privileges.forEach((commandId, list) -> {
			DataObject entry = DataObject.empty();
			entry.put("id", commandId);
			entry.put("permissions", DataArray.fromCollection(list));
			array.add(entry);
		});

		return array.toJson();
	}

	public void updateGuildPrivileges(Guild guild) {
		final Map<String, Collection<? extends CommandPrivilege>> privileges = guildToPrivilegesMap.get(guild.getIdLong());

		guild.updateCommandPrivileges(privileges).queue(x -> {
			final Path path = getTempPath(guild.getId() + "Privileges.json");
			try {
				Files.write(path, getGuildCommandPrivilegesBytes(guild), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
			} catch (IOException e) {
				if (LOGGER.isErrorEnabled()) {
					LOGGER.error("An exception occurred while temporarily saving guild ({} ({})) command privileges in '{}'", guild.getName(), guild.getId(), path.toAbsolutePath(), e);
				} else {
					System.err.printf("An exception occurred while temporarily saving guild (%s (%d)) command privileges in '%s'%n", guild.getName(), guild.getIdLong(), path.toAbsolutePath());
					e.printStackTrace();
				}
			}
		});
	}

	private List<Command> getGuildCommands(Guild guild) {
		return guildToCommandsMap.computeIfAbsent(guild.getIdLong(),
				x -> guild.retrieveCommands().complete());
	}
}