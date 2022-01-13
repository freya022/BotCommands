package com.freya02.botcommands.internal.application.slash.autocomplete.caches;

import com.freya02.botcommands.internal.RunnableEx;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import net.dv8tion.jda.api.interactions.commands.Command;

import java.util.List;
import java.util.function.Consumer;

public class ConstantByKeyAutocompletionCache extends AbstractAutocompletionCache {
	protected final Cache<String, List<Command.Choice>> cache;

	public ConstantByKeyAutocompletionCache(long maximumWeight) {
		cache = Caffeine.newBuilder()
				.maximumWeight(maximumWeight)
//				.evictionListener((key, value, cause) -> {
//					LOGGER.trace("Evicted autocomplete key '{}', of size {} for cause {}, maximum weight: {}", key, getEntrySize(key, value), cause.name(), maximumWeight);
//				})
				//Weight by the sum of the choice value lengths
				.weigher(this::getEntrySize)
				.build();
	}

	@SuppressWarnings("unchecked")
	private int getEntrySize(Object k, Object v) {
		int sum = ((String) k).length();

		final List<Command.Choice> choices = (List<Command.Choice>) v;
		for (final Command.Choice c : choices) {
			sum += c.getName().length() + c.getAsString().length();
		}

		return sum;
	}

	@Override
	public void retrieveAndCall(String stringOption, Consumer<List<Command.Choice>> choiceCallback, RunnableEx valueComputer) throws Exception {
		final List<Command.Choice> cachedValue = cache.getIfPresent(stringOption);

		if (cachedValue != null) {
			choiceCallback.accept(cachedValue);
		} else {
			valueComputer.run(); //Choice callback is called by valueComputer
		}
	}

	@Override
	public void put(String stringOption, List<Command.Choice> choices) {
		cache.put(stringOption, choices);
	}
}
