package com.freya02.botcommands.internal.application.slash.autocomplete.caches;

import com.freya02.botcommands.api.application.slash.autocomplete.AutocompletionCacheMode;
import com.freya02.botcommands.internal.RunnableEx;
import net.dv8tion.jda.api.interactions.commands.Command;

import java.util.List;
import java.util.function.Consumer;

public abstract class AbstractAutocompletionCache {
	public static AbstractAutocompletionCache fromMode(AutocompletionCacheMode cacheMode, long maxCacheSizeKb) {
		return switch (cacheMode) {
			case CONSTANT_BY_KEY -> new ConstantByKeyAutocompletionCache(maxCacheSizeKb);
			case NO_CACHE -> new NoCacheAutocompletion();
		};
	}

	public abstract void retrieveAndCall(String stringOption, Consumer<List<Command.Choice>> choiceCallback, RunnableEx valueComputer) throws Exception;

	public abstract void put(String stringOption, List<Command.Choice> choices);

	public abstract void invalidate();
}
