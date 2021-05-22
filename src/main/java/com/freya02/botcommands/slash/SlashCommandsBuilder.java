package com.freya02.botcommands.slash;

import com.freya02.botcommands.Emoji;
import com.freya02.botcommands.*;
import com.freya02.botcommands.annotation.Optional;
import com.freya02.botcommands.slash.annotations.Choice;
import com.freya02.botcommands.slash.annotations.Choices;
import com.freya02.botcommands.slash.annotations.JdaSlashCommand;
import com.freya02.botcommands.slash.annotations.Option;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

public final class SlashCommandsBuilder {
	private static final Logger LOGGER = Logging.getLogger();
	private final BContextImpl context;

	public SlashCommandsBuilder(@NotNull BContextImpl context) {
		this.context = context;
	}

	public void processSlashCommand(SlashCommand slashCommand) {
		for (Method method : slashCommand.getClass().getDeclaredMethods()) {
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

				context.addSlashCommand(info.getPath(), info);
			}
		}
	}

	public void postProcess(List<Long> slashGuildIds) {
		final Map<String, CommandData> guildMap = new HashMap<>();
		final Map<String, SubcommandGroupData> guildGroupMap = new HashMap<>();

		final Map<String, CommandData> globalMap = new HashMap<>();
		final Map<String, SubcommandGroupData> globalGroupMap = new HashMap<>();

		context.getSlashCommands().stream().sorted(Comparator.comparingInt(SlashCommandInfo::getPathComponents)).forEachOrdered(info -> {
			final Map<String, CommandData> map = info.isGuildOnly() ? guildMap : globalMap;
			final Map<String, SubcommandGroupData> groupMap = info.isGuildOnly() ? guildGroupMap : globalGroupMap;

			final String path = info.getPath();
			if (info.getPathComponents() == 1) {
				//Standard command
				final CommandData rightCommand = new CommandData(info.getName(), info.getDescription());
				map.put(path, rightCommand);

				for (OptionData option : getMethodOptions(info)) {
					rightCommand.addOption(option);
				}
			} else if (info.getPathComponents() == 2) {
				//Subcommand of a command
				final String parent = getParent(path);
				final CommandData commandData = map.computeIfAbsent(parent, s -> new CommandData(getName(parent), "we can't see this rite ?"));

				final SubcommandData rightCommand = new SubcommandData(info.getName(), info.getDescription());
				commandData.addSubcommand(rightCommand);

				for (OptionData option : getMethodOptions(info)) {
					rightCommand.addOption(option);
				}
			} else if (info.getPathComponents() == 3) {
				final String namePath = getParent(getParent(path));
				final String parentPath = getParent(path);
				final SubcommandGroupData groupData = groupMap.computeIfAbsent(parentPath, gp -> {
					final CommandData nameData = new CommandData(getName(namePath), "we can't see r-right ?");
					map.put(getName(namePath), nameData);

					final SubcommandGroupData groupDataTmp = new SubcommandGroupData(getName(parentPath), "we can't see r-right ?");
					nameData.addSubcommandGroup(groupDataTmp);

					return groupDataTmp;
				});

				final SubcommandData rightCommand = new SubcommandData(info.getName(), info.getDescription());
				groupData.addSubcommand(rightCommand);

				for (OptionData option : getMethodOptions(info)) {
					rightCommand.addOption(option);
				}
			} else {
				throw new IllegalStateException("A slash command with more than 4 names got registered");
			}
		});

		context.getJDA().updateCommands().addCommands(globalMap.values()).queue();

		final List<Guild> guilds = new ArrayList<>(context.getJDA().getGuilds());
		if (slashGuildIds != null) {
			guilds.removeIf(g -> !slashGuildIds.contains(g.getIdLong()));
		}

		for (Guild guild : guilds) {
			guild.updateCommands()
					.addCommands(guildMap.values())
					.queue(v -> LOGGER.trace("Updated commands for {}", guild.getName()));
		}
	}

	private static List<OptionData> getMethodOptions(SlashCommandInfo info) {
		final List<OptionData> list = new ArrayList<>();

		Parameter[] parameters = info.getCommandMethod().getParameters();
		for (int i = 1, parametersLength = parameters.length; i < parametersLength; i++) {
			Parameter parameter = parameters[i];

			final Class<?> type = parameter.getType();
			final Option option = parameter.getAnnotation(Option.class);

			final String name;
			final String description;
			if (option == null) {
				if (!parameter.isNamePresent())
					throw new RuntimeException("Parameter name cannot be deduced as the slash command option's name is not specified on: " + parameter);

				name = parameter.getName();
				description = "No description";
			} else {
				if (option.name().isBlank()) {
					if (!parameter.isNamePresent())
						throw new RuntimeException("Parameter name cannot be deduced as the slash command option's name is not specified on: " + parameter);

					name = parameter.getName();
				} else {
					name = option.name();
				}

				if (option.description().isBlank()) {
					description = "No description";
				} else {
					description = option.description();
				}
			}

			final OptionData data;
			if (type == User.class || type == Member.class) {
				data = new OptionData(OptionType.USER, name, description);
			} else if (type == Role.class) {
				data = new OptionData(OptionType.ROLE, name, description);
			} else if (type == TextChannel.class) {
				data = new OptionData(OptionType.CHANNEL, name, description);
			} else if (type == IMentionable.class) {
				data = new OptionData(OptionType.MENTIONABLE, name, description);
			} else if (type == boolean.class) {
				data = new OptionData(OptionType.BOOLEAN, name, description);
			} else if (type == long.class) {
				data = new OptionData(OptionType.INTEGER, name, description);

				final Choices choices = parameter.getAnnotation(Choices.class);
				if (choices != null) {
					for (Choice choice : choices.value()) {
						data.addChoice(choice.name(), choice.intValue());
					}
				}
			} else if (type == String.class || type == Emote.class || type == Emoji.class || type == EmojiOrEmote.class) {
				data = new OptionData(OptionType.STRING, name, description);

				final Choices choices = parameter.getAnnotation(Choices.class);
				if (choices != null) {
					for (Choice choice : choices.value()) {
						data.addChoice(choice.name(), choice.value());
					}
				}
			} else {
				throw new IllegalArgumentException("Unknown slash command option: " + type.getName());
			}

			list.add(data);

			data.setRequired(!parameter.isAnnotationPresent(Optional.class));
		}

		return list;
	}

	private static String getName(String path) {
		final int i = path.indexOf('/');
		if (i == -1) return path;

		return path.substring(i);
	}

	@Nonnull
	private static String getParent(String path) {
		return path.substring(0, path.lastIndexOf('/'));
	}
}