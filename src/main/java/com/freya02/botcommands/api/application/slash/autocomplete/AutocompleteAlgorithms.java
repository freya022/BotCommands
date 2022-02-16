package com.freya02.botcommands.api.application.slash.autocomplete;

import com.freya02.botcommands.internal.utils.StringUtils;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import me.xdrop.fuzzywuzzy.ToStringFunction;
import me.xdrop.fuzzywuzzy.model.BoundExtractedResult;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.AutoCompleteQuery;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * Class which contains the algorithms used by the autocompletion handlers
 * <br>You might use them if for example you want to provide your own list of choices directly, while still having some sort of relevance sorting
 */
public class AutocompleteAlgorithms {
	/**
	 * Sorts the item with fuzzing matching, the {@value OptionData#MAX_CHOICES} most relevant results should appear at the top
	 * <br>Additionally this removes items which do not start with the query string
	 *
	 * @param items            The items to sort
	 * @param toStringFunction The function to transform an item into a String
	 * @param event            The autocompletion event
	 * @param <T>              Type of the items
	 * @return A list of extract results with the scores of each item
	 */
	public static <T> List<BoundExtractedResult<T>> fuzzyMatchingWithContinuity(Collection<T> items, ToStringFunction<T> toStringFunction, CommandAutoCompleteInteractionEvent event) {
		final AutoCompleteQuery autoCompleteQuery = event.getFocusedOption();

		final String query = autoCompleteQuery.getValue();
		final List<T> list = items
				.stream()
				.filter(s -> StringUtils.startsWithIgnoreCase(toStringFunction.apply(s), query))
				.sorted(Comparator.comparing(toStringFunction::apply))
				.toList();

		return FuzzySearch.extractTop(query,
				list,
				toStringFunction,
				FuzzySearch::ratio,
				OptionData.MAX_CHOICES);
	}

	/**
	 * Sorts the item with fuzzing matching, the {@value OptionData#MAX_CHOICES} most relevant results should appear at the top
	 * <br>This algorithm is different from {@link #fuzzyMatchingWithContinuity(Collection, ToStringFunction, CommandAutoCompleteInteractionEvent)}
	 *
	 * @param items            The items to sort
	 * @param toStringFunction The function to transform an item into a String
	 * @param event            The autocompletion event
	 * @param <T>              Type of the items
	 * @return A list of extract results with the scores of each item
	 */
	public static <T> List<BoundExtractedResult<T>> fuzzyMatching(Collection<T> items, ToStringFunction<T> toStringFunction, CommandAutoCompleteInteractionEvent event) {
		final List<T> list = items
				.stream()
				.sorted(Comparator.comparing(toStringFunction::apply))
				.toList();

		final AutoCompleteQuery autoCompleteQuery = event.getFocusedOption();
		//First sort the results by similarities but by taking into account an incomplete input
		final List<BoundExtractedResult<T>> bigLengthDiffResults = FuzzySearch.extractTop(autoCompleteQuery.getValue(),
				list,
				toStringFunction,
				FuzzySearch::partialRatio,
				OptionData.MAX_CHOICES);

		//Then sort the results by similarities but don't take length into account
		return FuzzySearch.extractTop(autoCompleteQuery.getValue(),
				bigLengthDiffResults.stream().map(BoundExtractedResult::getReferent).toList(),
				toStringFunction,
				FuzzySearch::ratio,
				OptionData.MAX_CHOICES);
	}
}
