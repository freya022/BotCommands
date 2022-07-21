package com.freya02.botcommands.internal.application.slash.autocomplete.caches;

import com.freya02.botcommands.internal.ConsumerEx;
import com.freya02.botcommands.internal.application.slash.autocomplete.CompositeAutocompletionKey;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;

import java.util.List;
import java.util.function.Consumer;

public class NoCacheAutocompletion extends AbstractAutocompletionCache {
	@Override
	public void retrieveAndCall(CommandAutoCompleteInteractionEvent event, Consumer<List<Command.Choice>> choiceCallback, ConsumerEx<CompositeAutocompletionKey> valueComputer) throws Exception {
		valueComputer.accept(null); //Always compute the value, the result gets replied by the computer
	}

	@Override
	public void put(CompositeAutocompletionKey key, List<Command.Choice> choices) {
		//Don't cache
	}

	@Override
	public void invalidate() {
		//No cache to invalidate
	}
}
