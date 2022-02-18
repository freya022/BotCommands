package com.freya02.botcommands.internal.application.slash.autocomplete.caches;

import com.freya02.botcommands.api.application.slash.autocomplete.annotations.CacheAutocompletion;
import com.freya02.botcommands.internal.ApplicationOptionData;
import com.freya02.botcommands.internal.application.slash.SlashCommandInfo;
import com.freya02.botcommands.internal.application.slash.autocomplete.AutocompleteCommandParameter;
import com.freya02.botcommands.internal.application.slash.autocomplete.AutocompletionHandlerInfo;
import com.freya02.botcommands.internal.application.slash.autocomplete.CompositeAutocompletionKey;
import com.freya02.botcommands.internal.utils.Utils;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.ToLongFunction;

public abstract class BaseAutocompletionCache extends AbstractAutocompletionCache {
	private final ToLongFunction<CommandAutoCompleteInteractionEvent> guildFunction;
	private final ToLongFunction<CommandAutoCompleteInteractionEvent> channelFunction;
	private final ToLongFunction<CommandAutoCompleteInteractionEvent> userFunction;

	protected BaseAutocompletionCache(@NotNull CacheAutocompletion cacheAutocompletion) {
		guildFunction = cacheAutocompletion.guildLocal()
				? (e -> e.getGuild() != null ? e.getGuild().getIdLong() : 0)
				: (e -> 0);

		channelFunction = cacheAutocompletion.channelLocal()
				? (e -> e.getChannel() != null ? e.getChannel().getIdLong() : 0)
				: (e -> 0);

		userFunction = cacheAutocompletion.userLocal()
				? (e -> e.getUser().getIdLong())
				: (e -> 0);
	}

	private String[] getCompositeOptionValues(AutocompletionHandlerInfo info,
	                                          SlashCommandInfo slashCommand,
	                                          CommandAutoCompleteInteractionEvent event) {
		final List<String> optionValues = new ArrayList<>();
		optionValues.add(event.getFocusedOption().getValue());

		int optionIndex = 0;
		final List<String> optionNames = event.getGuild() != null ? slashCommand.getLocalizedOptions(event.getGuild()) : null;
		for (final AutocompleteCommandParameter parameter : info.getParameters()) {
			final ApplicationOptionData applicationOptionData = parameter.getApplicationOptionData();

			if (parameter.isOption()) {
				final String optionName = optionNames == null ? applicationOptionData.getEffectiveName() : optionNames.get(optionIndex);
				if (optionName == null) {
					throw new IllegalArgumentException(String.format("Option name #%d (%s) could not be resolved for %s", optionIndex, applicationOptionData.getEffectiveName(), Utils.formatMethodShort(info.getMethod())));
				}

				optionIndex++;

				if (parameter.isCompositeKey()) {
					final OptionMapping option = event.getOption(optionName);

					if (option == null) {
						optionValues.add("null");
					} else if (!event.getFocusedOption().getName().equals(optionName)) { //Only add the options other than the focused one, since it's already there, saves us from an HashSet
						optionValues.add(option.getAsString());
					}
				}
			}
		}

		return optionValues.toArray(new String[0]);
	}

	protected CompositeAutocompletionKey getCompositeKey(AutocompletionHandlerInfo info, SlashCommandInfo slashCommand, CommandAutoCompleteInteractionEvent event) {
		final String[] compositeOptionValues = getCompositeOptionValues(info, slashCommand, event);

		return new CompositeAutocompletionKey(compositeOptionValues,
				guildFunction.applyAsLong(event),
				channelFunction.applyAsLong(event),
				userFunction.applyAsLong(event));
	}
}
