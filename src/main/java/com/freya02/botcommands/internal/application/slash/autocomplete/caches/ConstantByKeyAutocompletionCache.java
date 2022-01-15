package com.freya02.botcommands.internal.application.slash.autocomplete.caches;

import com.freya02.botcommands.internal.RunnableEx;
import com.freya02.botcommands.internal.application.slash.autocomplete.CompositeAutocompletionKey;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import net.dv8tion.jda.api.interactions.commands.Command;

import java.util.List;
import java.util.function.Consumer;

public class ConstantByKeyAutocompletionCache extends AbstractAutocompletionCache {
	private final Cache<CompositeAutocompletionKey, List<Command.Choice>> cache;

	public ConstantByKeyAutocompletionCache(long maximumWeightKb) {
		cache = Caffeine.newBuilder()
				.maximumWeight(maximumWeightKb * 1024)
//				.evictionListener((key, value, cause) -> {
//					LOGGER.trace("Evicted autocomplete key '{}', of size {} for cause {}, maximum weight: {}", key, getEntrySize(key, value), cause.name(), maximumWeight);
//				})
				//Weight by the sum of the choice value lengths
				.weigher(this::getEntrySize)
				.build();
	}

	@SuppressWarnings("unchecked")
	private int getEntrySize(Object k, Object v) {
		int sum = ((CompositeAutocompletionKey) k).length();

		final List<Command.Choice> choices = (List<Command.Choice>) v;
		for (final Command.Choice c : choices) {
			sum += c.getName().length() + c.getAsString().length();
		}

		return sum;
	}

	@Override
	public void retrieveAndCall(CompositeAutocompletionKey key, Consumer<List<Command.Choice>> choiceCallback, RunnableEx valueComputer) throws Exception {
		final List<Command.Choice> cachedValue = cache.getIfPresent(key);

		if (cachedValue != null) {
			choiceCallback.accept(cachedValue);
		} else {
			valueComputer.run(); //Choice callback is called by valueComputer
		}
	}

	@Override
	public void put(CompositeAutocompletionKey key, List<Command.Choice> choices) {
		cache.put(key, choices);
	}

	@Override
	public void invalidate() {
		cache.invalidateAll();
	}
}
