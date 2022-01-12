package com.freya02.botcommands.internal.application.slash.autocomplete.suppliers;

import com.freya02.botcommands.internal.application.slash.SlashCommandInfo;
import com.freya02.botcommands.internal.application.slash.autocomplete.AutocompletionHandlerInfo;
import com.freya02.botcommands.internal.application.slash.autocomplete.ChoiceSupplier;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import me.xdrop.fuzzywuzzy.model.ExtractedResult;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.Collection;
import java.util.List;

public class ChoiceSupplierStringFuzzy implements ChoiceSupplier {
	private final AutocompletionHandlerInfo handlerInfo;

	public ChoiceSupplierStringFuzzy(AutocompletionHandlerInfo handlerInfo) {
		this.handlerInfo = handlerInfo;
	}

	@Override
	public List<Command.Choice> apply(SlashCommandInfo slashCommand, CommandAutoCompleteInteractionEvent event, Collection<?> collection) throws Exception {
		final List<String> list = collection
				.stream()
				.map(Object::toString)
				.sorted()
				.toList();

		final OptionMapping optionMapping = event.getFocusedOption();
		//First sort the results by similarities but by taking into account an incomplete input
		final List<ExtractedResult> bigLengthDiffResults = FuzzySearch.extractTop(optionMapping.getAsString(),
				list,
				FuzzySearch::partialRatio,
				handlerInfo.getMaxChoices());

		//Then sort the results by similarities but don't take length into account
		final List<ExtractedResult> similarities = FuzzySearch.extractTop(optionMapping.getAsString(),
				bigLengthDiffResults.stream().map(ExtractedResult::getString).toList(),
				FuzzySearch::ratio,
				handlerInfo.getMaxChoices());

		return similarities.stream()
				.limit(handlerInfo.getMaxChoices())
				.map(c -> AutocompletionHandlerInfo.getChoice(optionMapping, c.getString()))
				.toList();
	}
}
