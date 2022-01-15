package com.freya02.botcommands.internal.application.slash.autocomplete.suppliers;

import com.freya02.botcommands.internal.application.slash.SlashCommandInfo;
import com.freya02.botcommands.internal.application.slash.autocomplete.AutocompletionHandlerInfo;
import com.freya02.botcommands.internal.application.slash.autocomplete.ChoiceSupplier;
import com.freya02.botcommands.internal.utils.Utils;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import me.xdrop.fuzzywuzzy.model.ExtractedResult;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.AutoCompleteQuery;
import net.dv8tion.jda.api.interactions.commands.Command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class ChoiceSupplierStringContinuity implements ChoiceSupplier {
	private final AutocompletionHandlerInfo handlerInfo;

	public ChoiceSupplierStringContinuity(AutocompletionHandlerInfo handlerInfo) {
		this.handlerInfo = handlerInfo;
	}

	@Override
	public List<Command.Choice> apply(SlashCommandInfo slashCommand, CommandAutoCompleteInteractionEvent event, Collection<?> collection) throws Exception {
		final AutoCompleteQuery autoCompleteQuery = event.getFocusedOption();

		final String query = autoCompleteQuery.getValue();
		final List<String> list = collection
				.stream()
				.map(Object::toString)
				.filter(s -> Utils.startsWithIgnoreCase(s, query))
				.sorted()
				.collect(Collectors.toCollection(ArrayList::new));

		final List<ExtractedResult> results = FuzzySearch.extractTop(query,
				list,
				FuzzySearch::ratio,
				handlerInfo.getMaxChoices());

		return results.stream()
				.limit(handlerInfo.getMaxChoices())
				.map(c -> AutocompletionHandlerInfo.getChoice(autoCompleteQuery.getType(), c.getString()))
				.toList();
	}
}
