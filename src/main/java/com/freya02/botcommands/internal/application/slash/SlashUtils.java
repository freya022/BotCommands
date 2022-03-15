package com.freya02.botcommands.internal.application.slash;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.SettingsProvider;
import com.freya02.botcommands.api.parameters.SlashParameterResolver;
import com.freya02.botcommands.internal.ApplicationOptionData;
import com.freya02.botcommands.internal.application.ApplicationCommandInfo;
import com.freya02.botcommands.internal.parameters.channels.ChannelResolver;
import com.freya02.botcommands.internal.utils.Utils;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

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

	public static List<OptionData> getMethodOptions(@NotNull BContext context, @Nullable Guild guild, @NotNull SlashCommandInfo info) {
		final List<OptionData> list = new ArrayList<>();
		final List<List<Command.Choice>> optionsChoices = getOptionChoices(context, guild, info);

		int i = 1;
		for (SlashCommandParameter parameter : info.getParameters()) {
			if (!parameter.isOption()) continue;

			final ApplicationOptionData applicationOptionData = parameter.getApplicationOptionData();

			final String name = parameter.getApplicationOptionData().getEffectiveName();
			final String description = parameter.getApplicationOptionData().getEffectiveDescription();

			final SlashParameterResolver resolver = parameter.getResolver();
			final OptionType optionType = resolver.getOptionType();
			final OptionData data = new OptionData(optionType, name, description);

			if (optionType == OptionType.CHANNEL) {
				//If there are no specified channel types, then try to get the channel type from AbstractChannelResolver
				// Otherwise set the channel types of the parameter, if available
				if (parameter.getChannelTypes().isEmpty() && resolver instanceof ChannelResolver channelResolver) {
					final EnumSet<ChannelType> channelTypes = channelResolver.getChannelTypes();

					data.setChannelTypes(channelTypes);
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
					throw new IllegalArgumentException("Slash command parameter #" + i + " of " + Utils.formatMethodShort(info.getMethod()) + " does not support autocompletion");
				}

				data.setAutoComplete(true);
			}

			if (optionType.canSupportChoices()) {
				Collection<Command.Choice> choices = null;

				//optionChoices might just be empty
				// choices of the option might also be empty as an empty list might be generated
				// do not add choices if it's empty, to not trigger checks
				if (optionsChoices.size() >= i && !optionsChoices.get(i - 1).isEmpty()) {
					choices = optionsChoices.get(i - 1);
				} else if (!resolver.getPredefinedChoices().isEmpty()) {
					choices = resolver.getPredefinedChoices();
				}

				if (choices != null) {
					if (applicationOptionData.hasAutocompletion()) {
						throw new IllegalArgumentException("Slash command parameter #" + i + " of " + Utils.formatMethodShort(info.getMethod()) + " cannot have autocompletion and choices at the same time");
					}

					data.addChoices(choices);
				}
			}

			data.setRequired(!parameter.isOptional());

			list.add(data);

			i++;
		}

		return list;
	}

	@NotNull
	public static List<List<Command.Choice>> getOptionChoices(BContext context, @Nullable Guild guild, ApplicationCommandInfo info) {
		List<List<Command.Choice>> optionsChoices = new ArrayList<>();

		final int count = info.getParameters().getOptionCount();
		for (int optionIndex = 0; optionIndex < count; optionIndex++) {
			optionsChoices.add(getChoicesForCommandOption(context, guild, info, optionIndex));
		}

		return optionsChoices;
	}

	@NotNull
	private static List<Command.Choice> getChoicesForCommandOption(BContext context, @Nullable Guild guild, ApplicationCommandInfo info, int optionIndex) {
		final List<Command.Choice> choices = info.getInstance().getOptionChoices(guild, info.getPath(), optionIndex);

		if (choices.isEmpty()) {
			final SettingsProvider settingsProvider = context.getSettingsProvider();

			if (settingsProvider != null) {
				return settingsProvider.getOptionChoices(guild, info.getPath(), optionIndex);
			}
		}

		return choices;
	}
}
