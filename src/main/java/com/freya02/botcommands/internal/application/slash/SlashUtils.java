package com.freya02.botcommands.internal.application.slash;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.SettingsProvider;
import com.freya02.botcommands.internal.ApplicationOptionData;
import com.freya02.botcommands.internal.application.ApplicationCommandInfo;
import com.freya02.botcommands.internal.application.ApplicationCommandParameter;
import com.freya02.botcommands.internal.application.LocalizedCommandData;
import com.freya02.botcommands.internal.parameters.channels.AbstractChannelResolver;
import com.freya02.botcommands.internal.utils.Utils;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
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
			for (Command.Option option : command.getOptions()) {
				joiner.add(option.getType().name());
			}

			sb.append(" - ").append(command.getName()).append(" ").append(joiner).append("\n");
		}
	}

	public static List<OptionData> getLocalizedMethodOptions(@NotNull SlashCommandInfo info, @NotNull LocalizedCommandData localizedCommandData) {
		final List<OptionData> list = new ArrayList<>();
		final List<LocalizedOption> optionNames = getLocalizedOptions(info, localizedCommandData);
		final List<List<Command.Choice>> optionsChoices = getAllOptionsLocalizedChoices(localizedCommandData);

		final long optionParamCount = info.getParameters().getOptionCount();
		Checks.check(optionNames.size() == optionParamCount, "Slash command has %s options but has %d parameters (after the event) @ %s, you should check if you return the correct number of localized strings", optionNames, optionParamCount - 1, Utils.formatMethodShort(info.getCommandMethod()));

		int i = 1;
		for (SlashCommandParameter parameter : info.getParameters()) {
			if (!parameter.isOption()) continue;

			final ApplicationOptionData applicationOptionData = parameter.getApplicationOptionData();

			final String name = optionNames.get(i - 1).getName();
			final String description = optionNames.get(i - 1).getDescription();

			final OptionType optionType = parameter.getResolver().getOptionType();
			final OptionData data = new OptionData(optionType, name, description);

			if (optionType == OptionType.CHANNEL) {
				//If there are no specified channel types, then try to get the channel type from AbstractChannelResolver
				// Otherwise set the channel types of the parameter, if available
				if (parameter.getChannelTypes().isEmpty() && parameter.getResolver() instanceof AbstractChannelResolver channelResolver) {
					data.setChannelTypes(channelResolver.getChannelType());
				} else if (!parameter.getChannelTypes().isEmpty()) {
					data.setChannelTypes(parameter.getChannelTypes());
				}
			} else if (optionType == OptionType.INTEGER) {
				data.setMinValue(parameter.getMinValue().longValue());
				data.setMaxValue(parameter.getMaxValue().longValue());
			} else if (optionType == OptionType.NUMBER) {
				data.setMinValue(parameter.getMinValue().doubleValue());
				data.setMaxValue(parameter.getMaxValue().doubleValue());
			}

			if (applicationOptionData.hasAutocompletion()) {
				if (!optionType.canSupportChoices()) {
					throw new IllegalArgumentException("Slash command parameter #" + i + " of " + Utils.formatMethodShort(info.getCommandMethod()) + " does not support autocompletion");
				}

				data.setAutoComplete(true);
			}

			if (optionType.canSupportChoices()) {
				//optionChoices might just be empty
				// choices of the option might also be empty as an empty list might be generated
				// do not add choices if it's empty, to not trigger checks
				if (optionsChoices.size() >= i && !optionsChoices.get(i - 1).isEmpty()) {
					if (applicationOptionData.hasAutocompletion()) {
						throw new IllegalArgumentException("Slash command parameter #" + i + " of " + Utils.formatMethodShort(info.getCommandMethod()) + " cannot have autocompletion and choices at the same time");
					}

					data.addChoices(optionsChoices.get(i - 1));
				}
			}

			data.setRequired(!parameter.isOptional());

			list.add(data);

			i++;
		}

		return list;
	}

	@NotNull
	public static List<List<Command.Choice>> getNotLocalizedChoices(BContext context, @Nullable Guild guild, ApplicationCommandInfo info) {
		List<List<Command.Choice>> optionsChoices = new ArrayList<>();

		final int count = info.getParameters().getOptionCount();
		for (int optionIndex = 0; optionIndex < count; optionIndex++) {
			optionsChoices.add(getNotLocalizedChoicesForCommand(context, guild, info, optionIndex));
		}

		return optionsChoices;
	}

	@NotNull
	private static List<Command.Choice> getNotLocalizedChoicesForCommand(BContext context, @Nullable Guild guild, ApplicationCommandInfo info, int optionIndex) {
		final List<Command.Choice> choices = info.getInstance().getOptionChoices(guild, info.getPath(), optionIndex);

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
	private static List<List<Command.Choice>> getAllOptionsLocalizedChoices(@Nullable LocalizedCommandData localizedCommandData) {
		return localizedCommandData == null
				? Collections.emptyList() //Here choices are only obtainable via the localized data as the annotations were removed.
				: Objects.requireNonNullElseGet(localizedCommandData.getLocalizedOptionChoices(), Collections::emptyList);
	}
}
