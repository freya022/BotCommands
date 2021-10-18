package com.freya02.botcommands.internal.application.slash;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.SettingsProvider;
import com.freya02.botcommands.api.parameters.ParameterResolvers;
import com.freya02.botcommands.internal.ApplicationOptionData;
import com.freya02.botcommands.internal.application.ApplicationCommandInfo;
import com.freya02.botcommands.internal.application.ApplicationCommandParameter;
import com.freya02.botcommands.internal.application.LocalizedCommandData;
import com.freya02.botcommands.internal.utils.Utils;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.SlashCommand;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.internal.utils.Checks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

import static com.freya02.botcommands.internal.application.LocalizedCommandData.LocalizedOption;

public class SlashUtils {
	public static void appendCommands(List<Command> commands, StringBuilder sb) {
		for (Command command : commands) {
			final StringJoiner joiner = new StringJoiner("] [", "[", "]").setEmptyValue("");
			if (command instanceof SlashCommand) {
				for (SlashCommand.Option option : ((SlashCommand) command).getOptions()) {
					joiner.add(option.getType().name());
				}
			}

			sb.append(" - ").append(command.getName()).append(" ").append(joiner).append("\n");
		}
	}

	public static List<OptionData> getLocalizedMethodOptions(@NotNull SlashCommandInfo info, @NotNull LocalizedCommandData localizedCommandData) {
		final List<OptionData> list = new ArrayList<>();
		final List<LocalizedOption> optionNames = getLocalizedOptions(info, localizedCommandData);
		final List<List<SlashCommand.Choice>> optionsChoices = getAllOptionsLocalizedChoices(localizedCommandData);

		final long optionParamCount = info.getParameters().getOptionCount();
		Checks.check(optionNames.size() == optionParamCount, "Slash command has %s options but has %d parameters (after the event) @ %s, you should check if you return the correct number of localized strings", optionNames, optionParamCount - 1, Utils.formatMethodShort(info.getCommandMethod()));

		int i = 1;
		for (SlashCommandParameter parameter : info.getParameters()) {
			if (!parameter.isOption()) continue;

			final Class<?> boxedType = parameter.getBoxedType();
			final ApplicationOptionData applicationOptionData = parameter.getApplicationOptionData();

			final String name = optionNames.get(i - 1).getName();
			final String description = optionNames.get(i - 1).getDescription();

			final OptionData data;
			if (boxedType == User.class || boxedType == Member.class) {
				data = new OptionData(OptionType.USER, name, description);
			} else if (boxedType == Role.class) {
				data = new OptionData(OptionType.ROLE, name, description);
			} else if (boxedType == TextChannel.class) {
				data = new OptionData(OptionType.CHANNEL, name, description);
			} else if (boxedType == IMentionable.class) {
				data = new OptionData(OptionType.MENTIONABLE, name, description);
			} else if (boxedType == Boolean.class) {
				data = new OptionData(OptionType.BOOLEAN, name, description);
			} else if (boxedType == Long.class) {
				data = new OptionData(OptionType.INTEGER, name, description);
			} else if (boxedType == Double.class) {
				data = new OptionData(OptionType.NUMBER, name, description);
			} else if (ParameterResolvers.exists(boxedType)) {
				data = new OptionData(OptionType.STRING, name, description);
			} else {
				throw new IllegalArgumentException("Unknown slash command option: " + boxedType.getName());
			}

			if (data.getType().canSupportChoices()) {
				//choices might just be empty
				if (optionsChoices.size() >= i) {
					data.addChoices(optionsChoices.get(i - 1));
				}
			}

			list.add(data);

			data.setRequired(!parameter.isOptional());

			i++;
		}

		return list;
	}

	@NotNull
	public static List<List<SlashCommand.Choice>> getNotLocalizedChoices(BContext context, @Nullable Guild guild, ApplicationCommandInfo info) {
		List<List<SlashCommand.Choice>> optionsChoices = new ArrayList<>();

		final int count = info.getParameters().getOptionCount();
		for (int optionIndex = 0; optionIndex < count; optionIndex++) {
			optionsChoices.add(getNotLocalizedChoicesForCommand(context, guild, info, optionIndex));
		}

		return optionsChoices;
	}

	@NotNull
	private static List<SlashCommand.Choice> getNotLocalizedChoicesForCommand(BContext context, @Nullable Guild guild, ApplicationCommandInfo info, int optionIndex) {
		final List<SlashCommand.Choice> choices = info.getInstance().getOptionChoices(guild, info.getPath(), optionIndex);

		if (choices.isEmpty()) {
			final SettingsProvider settingsProvider = context.getSettingsProvider();

			if (settingsProvider != null) {
				return settingsProvider.getOptionChoices(guild, info.getPath(), optionIndex);
			}
		}

		return choices;
	}

	@NotNull
	private static List<LocalizedOption> getLocalizedOptions(@NotNull SlashCommandInfo info, @Nullable LocalizedCommandData localizedCommandData) {
		return localizedCommandData == null
				? getNotLocalizedMethodOptions(info)
				: Objects.requireNonNullElseGet(localizedCommandData.getLocalizedOptionNames(), () -> getNotLocalizedMethodOptions(info));
	}

	@NotNull
	private static List<LocalizedOption> getNotLocalizedMethodOptions(@NotNull SlashCommandInfo info) {
		return info.getParameters()
				.stream()
				.filter(ApplicationCommandParameter::isOption)
				.map(ApplicationCommandParameter::getApplicationOptionData)
				.map(param -> new LocalizedOption(param.getEffectiveName(), param.getEffectiveDescription()))
				.collect(Collectors.toList());
	}

	@NotNull
	private static List<List<SlashCommand.Choice>> getAllOptionsLocalizedChoices(@Nullable LocalizedCommandData localizedCommandData) {
		return localizedCommandData == null
				? Collections.emptyList() //Here choices are only obtainable via the localized data as the annotations were removed.
				: Objects.requireNonNullElseGet(localizedCommandData.getLocalizedOptionChoices(), Collections::emptyList);
	}
}
