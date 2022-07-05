package com.freya02.botcommands.internal.application.slash.autocomplete.caches;

import com.freya02.botcommands.annotations.api.application.slash.autocomplete.annotations.CacheAutocompletion;
import com.freya02.botcommands.internal.ConsumerEx;
import com.freya02.botcommands.internal.application.slash.autocomplete.AutocompletionHandlerInfo;
import com.freya02.botcommands.internal.application.slash.autocomplete.CompositeAutocompletionKey;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;

import java.util.List;
import java.util.function.Consumer;

public abstract class AbstractAutocompletionCache {
	public static AbstractAutocompletionCache fromMode(AutocompletionHandlerInfo handlerInfo, CacheAutocompletion cacheAutocompletion) {
		if (cacheAutocompletion == null) return new NoCacheAutocompletion();

		return switch (cacheAutocompletion.cacheMode()) {
			case NO_CACHE -> new NoCacheAutocompletion();
			case CONSTANT_BY_KEY -> new ConstantByKeyAutocompletionCache(handlerInfo, cacheAutocompletion);
		};
	}

	public abstract void retrieveAndCall(CommandAutoCompleteInteractionEvent event, Consumer<List<Command.Choice>> choiceCallback, ConsumerEx<CompositeAutocompletionKey> valueComputer) throws Exception;

	public abstract void put(CompositeAutocompletionKey key, List<Command.Choice> choices);

	public abstract void invalidate();
}
