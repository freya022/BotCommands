package com.freya02.botcommands.internal.application.slash.autocomplete.caches;

import com.freya02.botcommands.annotations.api.application.slash.autocomplete.annotations.CacheAutocompletion;
import com.freya02.botcommands.internal.ConsumerEx;
import com.freya02.botcommands.internal.application.slash.autocomplete.AutocompletionHandlerInfo;
import com.freya02.botcommands.internal.application.slash.autocomplete.CompositeAutocompletionKey;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

public class ConstantByKeyAutocompletionCache extends BaseAutocompletionCache {
	private final AutocompletionHandlerInfo handlerInfo;
	private final Cache<CompositeAutocompletionKey, List<Command.Choice>> cache;

	public ConstantByKeyAutocompletionCache(AutocompletionHandlerInfo handlerInfo, @NotNull CacheAutocompletion cacheAutocompletion) {
		super(cacheAutocompletion);

		this.handlerInfo = handlerInfo;

		cache = Caffeine.newBuilder()
				.maximumWeight(cacheAutocompletion.cacheSize() * 1024)
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
	public void retrieveAndCall(CommandAutoCompleteInteractionEvent event, Consumer<List<Command.Choice>> choiceCallback, ConsumerEx<CompositeAutocompletionKey> valueComputer) throws Exception {
		final CompositeAutocompletionKey compositeKey = getCompositeKey(handlerInfo, event);
		final List<Command.Choice> cachedValue = cache.getIfPresent(compositeKey);

		if (cachedValue != null) {
			choiceCallback.accept(cachedValue);
		} else {
			valueComputer.accept(compositeKey); //Choice callback is called by valueComputer
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
