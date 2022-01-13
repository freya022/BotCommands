package com.freya02.botcommands.internal.application.slash.autocomplete.caches;

import com.freya02.botcommands.internal.RunnableEx;
import net.dv8tion.jda.api.interactions.commands.Command;

import java.util.List;
import java.util.function.Consumer;

public class ConstantAutocompletionCache extends AbstractAutocompletionCache {
	private List<Command.Choice> values = null;

	@Override
	public void retrieveAndCall(String stringOption, Consumer<List<Command.Choice>> choiceCallback, RunnableEx valueComputer) throws Exception {
		if (values != null) {
			choiceCallback.accept(values);
		} else {
			valueComputer.run(); //Choice callback is called by valueComputer
		}
	}

	@Override
	public void put(String stringOption, List<Command.Choice> choices) {
		this.values = choices;
	}
}
