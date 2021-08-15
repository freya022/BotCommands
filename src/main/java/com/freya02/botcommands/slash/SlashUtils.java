package com.freya02.botcommands.slash;

import com.freya02.botcommands.BContext;
import com.freya02.botcommands.SettingsProvider;
import com.freya02.botcommands.annotation.Optional;
import com.freya02.botcommands.parameters.ParameterResolvers;
import com.freya02.botcommands.slash.annotations.Option;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Parameter;
import java.util.*;

class SlashUtils {
	static void appendCommands(List<Command> commands, StringBuilder sb) {
		for (Command command : commands) {
			final StringJoiner joiner = new StringJoiner("] [", "[", "]").setEmptyValue("");
			for (Command.Option option : command.getOptions()) {
				joiner.add(option.getType().name());
			}

			sb.append(" - ").append(command.getName()).append(" ").append(joiner).append("\n");
		}
	}

	static List<OptionData> getMethodOptions(BContext context, Guild guild, SlashCommandInfo info) {
		final List<OptionData> list = new ArrayList<>();

		Parameter[] parameters = info.getCommandMethod().getParameters();
		for (int i = 1, parametersLength = parameters.length; i < parametersLength; i++) {
			Parameter parameter = parameters[i];

			final Class<?> type = parameter.getType();
			final Option option = parameter.getAnnotation(Option.class);

			final String name;
			final String description;
			if (option == null) {
				name = getOptionName(parameter, context, guild, info, i);
				description = "No description";
			} else {
				if (option.name().isBlank()) {
					name = getOptionName(parameter, context, guild, info, i);
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
			} else if (type == double.class) {
				data = new OptionData(OptionType.NUMBER, name, description);
			} else if (ParameterResolvers.exists(type)) {
				data = new OptionData(OptionType.STRING, name, description);
			} else {
				throw new IllegalArgumentException("Unknown slash command option: " + type.getName());
			}

			if (data.getType().canSupportChoices()) {
				final Collection<Command.Choice> choices = getLocalizedChoices(context, guild, info, i, name);
				
				//might just be empty
				data.addChoices(choices);
			}

			list.add(data);

			data.setRequired(!parameter.isAnnotationPresent(Optional.class));
		}

		return list;
	}

	@Nonnull
	private static Collection<Command.Choice> getLocalizedChoices(BContext context, Guild guild, SlashCommandInfo info, int optionIndex, String name) {
		Collection<Command.Choice> choices = info.getInstance().getCommandChoices(guild, info.getPath(), name, optionIndex);

		if (choices == null || choices.isEmpty()) {
			final SettingsProvider settingsProvider = context.getSettingsProvider();

			if (settingsProvider != null) {
				choices = settingsProvider.getCommandChoices(guild, info.getPath(), name, optionIndex);
			}
		}
		
		return choices == null ? Collections.emptyList() : choices;
	}

	@Nullable
	private static String getLocalizedOptionName(BContext context, Guild guild, SlashCommandInfo info, int optionIndex) {
		String name = info.getInstance().getOptionName(guild, info.getPath(), optionIndex);

		if (name == null || name.isBlank()) {
			final SettingsProvider settingsProvider = context.getSettingsProvider();

			if (settingsProvider != null) {
				name = settingsProvider.getOptionName(guild, info.getPath(), optionIndex);
			}
		}

		return name;
	}

	static String getLocalizedDescription(BContext context, Guild guild, SlashCommandInfo info) {
		String name = info.getInstance().getCommandDescription(guild, info.getPath());

		if (name == null || name.isBlank()) {
			final SettingsProvider settingsProvider = context.getSettingsProvider();

			if (settingsProvider != null) {
				name = settingsProvider.getCommandDescription(guild, info.getPath());
			}
		}

		return name;
	}

	static String getLocalizedPath(BContext context, Guild guild, SlashCommandInfo info) {
		String name = info.getInstance().getCommandName(guild, info.getPath());

		if (name == null || name.isBlank()) {
			final SettingsProvider settingsProvider = context.getSettingsProvider();

			if (settingsProvider != null) {
				name = settingsProvider.getCommandName(guild, info.getPath());
			}
		}

		return name;
	}

	private static String getOptionName(Parameter parameter, BContext context, Guild guild, SlashCommandInfo info, int optionIndex) {
		if (!parameter.isNamePresent())
			throw new RuntimeException("Parameter name cannot be deduced as the slash command option's name is not specified on: " + parameter);

		final String optionName = getLocalizedOptionName(context, guild, info, optionIndex);
		
		if (optionName != null && !optionName.isBlank()) {
			return optionName;
		}

		final String name = parameter.getName();
		final int nameLength = name.length();
		
		final StringBuilder optionNameBuilder = new StringBuilder(nameLength + 10); //I doubt you'd have a parameter long enough to have more than 10 underscores
		for (int i = 0; i < nameLength; i++) {
			final char c = name.charAt(i);

			if (Character.isUpperCase(c)) {
				optionNameBuilder.append('_').append(Character.toLowerCase(c));
			} else {
				optionNameBuilder.append(c);
			}
		}

		return optionNameBuilder.toString();
	}

	static String getPathName(String path) {
		final int i = path.indexOf('/');
		if (i == -1) return path;

		return path.substring(i + 1);
	}

	@Nonnull
	static String getPathParent(String path) {
		return path.substring(0, path.lastIndexOf('/'));
	}
}
