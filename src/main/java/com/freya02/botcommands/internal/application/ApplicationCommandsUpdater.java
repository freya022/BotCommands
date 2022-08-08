package com.freya02.botcommands.internal.application;

import com.freya02.botcommands.api.Logging;
import com.freya02.botcommands.api.application.CommandPath;
import com.freya02.botcommands.api.application.CommandScope;
import com.freya02.botcommands.api.builder.DebugBuilder;
import com.freya02.botcommands.internal.BContextImpl;
import com.freya02.botcommands.internal.application.context.message.MessageCommandInfo;
import com.freya02.botcommands.internal.application.context.user.UserCommandInfo;
import com.freya02.botcommands.internal.application.localization.BCLocalizationFunction;
import com.freya02.botcommands.internal.application.slash.SlashCommandInfo;
import com.freya02.botcommands.internal.application.slash.SlashUtils2;
import com.freya02.botcommands.internal.utils.Utils;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.*;
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationFunction;
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

import static com.freya02.botcommands.internal.application.slash.SlashUtils.appendCommands;

@Deprecated
public class ApplicationCommandsUpdater {
	private static final Logger LOGGER = Logging.getLogger();

	private final BContextImpl context;
	@Nullable private final Guild guild;
	private final boolean onlineCheck;

	private final Path commandsCachePath;

	private final ApplicationCommandDataMap map = new ApplicationCommandDataMap();
	private final Map<String, SubcommandGroupData> subcommandGroupDataMap = new HashMap<>();
	private final List<ApplicationCommandInfo> guildApplicationCommands;

	private final List<Command> commands = new ArrayList<>();

	private final Collection<CommandData> allCommandData;

	private ApplicationCommandsUpdater(@NotNull BContextImpl context, @Nullable Guild guild, boolean onlineCheck) throws IOException {
		this.context = context;
		this.guild = guild;
		this.onlineCheck = onlineCheck;

		this.commandsCachePath = guild == null
				? context.getApplicationCommandsCache().getGlobalCommandsPath()
				: context.getApplicationCommandsCache().getGuildCommandsPath(guild);

		Files.createDirectories(commandsCachePath.getParent());

		final CommandIdProcessor commandIdProcessor = guild == null ? null : new CommandIdProcessor(context);
		this.guildApplicationCommands = this.context.getApplicationCommandsContext()
				.getApplicationCommandInfoMap()
				.filterByGuild(this.context, this.guild, commandIdProcessor);

		computeCommands();

		this.allCommandData = map.getAllCommandData();

		//Apply localization
		final LocalizationFunction localizationFunction = new BCLocalizationFunction(context);
		for (CommandData commandData : allCommandData) {
			commandData.setLocalizationFunction(localizationFunction);
		}
	}

	public static ApplicationCommandsUpdater ofGlobal(@NotNull BContextImpl context, boolean onlineCheck) throws IOException {
		return new ApplicationCommandsUpdater(context, null, onlineCheck);
	}

	public static ApplicationCommandsUpdater ofGuild(@NotNull BContextImpl context, @NotNull Guild guild, boolean onlineCheck) throws IOException {
		return new ApplicationCommandsUpdater(context, guild, onlineCheck);
	}

	public List<ApplicationCommandInfo> getGuildApplicationCommands() {
		return guildApplicationCommands;
	}

	@Nullable
	public Guild getGuild() {
		return guild;
	}

	@Blocking
	public boolean shouldUpdateCommands() throws IOException {
		final byte[] oldBytes;

		if (onlineCheck) {
			commands.clear();
			commands.addAll((guild == null ? context.getJDA().retrieveCommands(true) : guild.retrieveCommands(true)).complete());
			final List<CommandData> discordCommandsData = commands.stream().map(CommandData::fromCommand).toList();

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

			if (DebugBuilder.isLogApplicationDiffsEnabled()) {
				LOGGER.trace("Old commands bytes: {}", new String(oldBytes));
				LOGGER.trace("New commands bytes: {}", new String(newBytes));
			}
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

	private void computeCommands() {
		computeSlashCommands(guildApplicationCommands);

		computeContextCommands(guildApplicationCommands, UserCommandInfo.class, Command.Type.USER);

		computeContextCommands(guildApplicationCommands, MessageCommandInfo.class, Command.Type.MESSAGE);
	}

	private void computeSlashCommands(List<ApplicationCommandInfo> guildApplicationCommands) {
		guildApplicationCommands.stream()
				.filter(a -> a instanceof SlashCommandInfo)
				.map(a -> (SlashCommandInfo) a)
				.forEachOrdered(info -> {
					final CommandPath commandPath = info.getPath();
					final String description = info.getDescription();

					try {
						final List<OptionData> methodOptions = SlashUtils2.getMethodOptions(info, context, guild);

						if (commandPath.getNameCount() == 1) {
							//Standard command
							final SlashCommandData rightCommand = Commands.slash(commandPath.getName(), description);
							map.put(Command.Type.SLASH, commandPath, rightCommand);

							rightCommand.addOptions(methodOptions);
							configureTopLevel(info, rightCommand);
						} else if (commandPath.getNameCount() == 2) {
							Checks.notNull(commandPath.getSubname(), "Subcommand name");

							final SlashCommandData commandData = (SlashCommandData) map.computeIfAbsent(Command.Type.SLASH, commandPath, x -> {
								final SlashCommandData tmpData = Commands.slash(commandPath.getName(), "No description (base name)");
								configureTopLevel(info, tmpData);

								return tmpData;
							});

							//Subcommand of a command
							final SubcommandData subcommandData = new SubcommandData(commandPath.getSubname(), description);
							subcommandData.addOptions(methodOptions);

							commandData.addSubcommands(subcommandData);
						} else if (commandPath.getNameCount() == 3) {
							Checks.notNull(commandPath.getGroup(), "Command group name");
							Checks.notNull(commandPath.getSubname(), "Subcommand name");

							final SubcommandGroupData groupData = getSubcommandGroup(Command.Type.SLASH, commandPath, x -> {
								final SlashCommandData commandData = Commands.slash(commandPath.getName(), "No description (base name)");
								configureTopLevel(info, commandData);

								return commandData;
							});

							final SubcommandData subcommandData = new SubcommandData(commandPath.getSubname(), description);
							subcommandData.addOptions(methodOptions);

							groupData.addSubcommands(subcommandData);
						} else {
							throw new IllegalStateException("A slash command with more than 4 path components got registered");
						}
					} catch (Exception e) {
						throw new RuntimeException("An exception occurred while processing command '" + commandPath + "' at " + Utils.formatMethodShort(info.getMethod()), e);
					}
				});
	}

	@SuppressWarnings("unchecked")
	private <T extends ApplicationCommandInfo> void computeContextCommands(List<ApplicationCommandInfo> guildApplicationCommands, Class<T> targetClazz, Command.Type type) {
		guildApplicationCommands.stream()
				.filter(a -> targetClazz.isAssignableFrom(a.getClass()))
				.map(a -> (T) a)
				.forEachOrdered(info -> {
					final CommandPath commandPath = info.getPath();

					try {
						if (commandPath.getNameCount() == 1) {
							//Standard command
							final CommandData rightCommand = Commands.context(type, commandPath.getName());
							map.put(type, commandPath, rightCommand);

							configureTopLevel(info, rightCommand);
						} else {
							throw new IllegalStateException("A " + type.name() + " command with more than 1 path component got registered");
						}
					} catch (Exception e) {
						throw new RuntimeException("An exception occurred while processing a " + type.name() + " command " + commandPath, e);
					}
				});
	}

	private void configureTopLevel(ApplicationCommandInfo info, CommandData rightCommand) {
		if (info.isDefaultLocked()) {
			rightCommand.setDefaultPermissions(DefaultMemberPermissions.DISABLED);
		} else if (!info.getUserPermissions().isEmpty()) {
			rightCommand.setDefaultPermissions(DefaultMemberPermissions.enabledFor(info.getUserPermissions()));
		}

		if (info.getScope() == CommandScope.GLOBAL_NO_DM) {
			rightCommand.setGuildOnly(true);
		}
	}

	private void thenAcceptGuild(List<Command> commands, @NotNull Guild guild) {
		for (Command command : commands) {
			context.getRegistrationListeners().forEach(l -> l.onGuildSlashCommandRegistered(this.guild, command));
		}

		this.commands.clear();
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

	//I am aware that the type is always Command.Type#SLASH, still use a parameter to mimic how ApplicationCommandMap functions and for future proof uses
	@SuppressWarnings("SameParameterValue")
	@NotNull
	private SubcommandGroupData getSubcommandGroup(Command.Type type, CommandPath path, Function<String, CommandData> baseCommandSupplier) {
		if (path.getGroup() == null)
			throw new IllegalArgumentException("Group component of command path is null at '" + path + "'");

		final SlashCommandData data = (SlashCommandData) map.computeIfAbsent(type, path, baseCommandSupplier);

		final CommandPath parent = path.getParent();
		if (parent == null) throw new IllegalStateException("A command path with less than 3 components was passed to #getSubcommandGroup");

		return subcommandGroupDataMap.computeIfAbsent(parent.getFullPath(), s -> {
			final SubcommandGroupData groupData = new SubcommandGroupData(path.getGroup(), "No description (group)");

			data.addSubcommandGroups(groupData);

			return groupData;
		});
	}
}