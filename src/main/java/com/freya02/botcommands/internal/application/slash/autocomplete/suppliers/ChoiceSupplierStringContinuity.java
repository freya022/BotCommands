package com.freya02.botcommands.internal.application.slash.autocomplete.suppliers;

import com.freya02.botcommands.api.application.slash.autocomplete.AutocompleteAlgorithms;
import com.freya02.botcommands.internal.application.slash.SlashCommandInfo;
import com.freya02.botcommands.internal.application.slash.autocomplete.AutocompletionHandlerInfo;
import com.freya02.botcommands.internal.application.slash.autocomplete.ChoiceSupplier;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.AutoCompleteQuery;
import net.dv8tion.jda.api.interactions.commands.Command;

import java.util.Collection;
import java.util.List;

public class ChoiceSupplierStringContinuity implements ChoiceSupplier {
	private final AutocompletionHandlerInfo handlerInfo;

	public ChoiceSupplierStringContinuity(AutocompletionHandlerInfo handlerInfo) {
		this.handlerInfo = handlerInfo;
	}

	@Override
	public List<Command.Choice> apply(SlashCommandInfo slashCommand, CommandAutoCompleteInteractionEvent event, Collection<?> collection) throws Exception {
		final AutoCompleteQuery autoCompleteQuery = event.getFocusedOption();

		return AutocompleteAlgorithms.fuzzyMatchingWithContinuity(collection, Object::toString, event)
				.stream()
				.limit(handlerInfo.getMaxChoices())
				.map(c -> AutocompletionHandlerInfo.getChoice(autoCompleteQuery.getType(), c.getString()))
				.toList();
	}
}
