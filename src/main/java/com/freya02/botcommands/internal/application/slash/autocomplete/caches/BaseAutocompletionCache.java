package com.freya02.botcommands.internal.application.slash.autocomplete.caches;

import com.freya02.botcommands.annotations.api.application.slash.autocomplete.annotations.CacheAutocompletion;
import com.freya02.botcommands.internal.application.slash.autocomplete.AutocompletionHandlerInfo;
import com.freya02.botcommands.internal.application.slash.autocomplete.CompositeAutocompletionKey;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.ToLongFunction;

@Deprecated
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
	                                          CommandAutoCompleteInteractionEvent event) {
		final List<String> optionValues = new ArrayList<>();

		//Identify the cached value by its command path too !
		optionValues.add(event.getName());
		if (event.getSubcommandGroup() != null) optionValues.add(event.getSubcommandGroup());
		if (event.getSubcommandName() != null) optionValues.add(event.getSubcommandName());

		optionValues.add(event.getFocusedOption().getValue());

		if (true) {
			throw new UnsupportedOperationException();
		}

//		for (final AutocompleteCommandParameter parameter : info.getParameters()) { //TODO
//			final ApplicationOptionData applicationOptionData = parameter.getApplicationOptionData();
//
//			if (parameter.isOption()) {
//				final String optionName = applicationOptionData.getEffectiveName();
//
//				if (parameter.isCompositeKey()) {
//					final OptionMapping option = event.getOption(optionName);
//
//					if (option == null) {
//						optionValues.add("null");
//					} else if (!event.getFocusedOption().getName().equals(optionName)) { //Only add the options other than the focused one, since it's already there, saves us from an HashSet
//						optionValues.add(option.getAsString());
//					}
//				}
//			}
//		}

		return optionValues.toArray(new String[0]);
	}

	protected CompositeAutocompletionKey getCompositeKey(AutocompletionHandlerInfo info, CommandAutoCompleteInteractionEvent event) {
		final String[] compositeOptionValues = getCompositeOptionValues(info, event);

		return new CompositeAutocompletionKey(compositeOptionValues,
				guildFunction.applyAsLong(event),
				channelFunction.applyAsLong(event),
				userFunction.applyAsLong(event));
	}
}
