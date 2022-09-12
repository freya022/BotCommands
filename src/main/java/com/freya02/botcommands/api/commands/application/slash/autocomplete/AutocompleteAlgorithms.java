package com.freya02.botcommands.api.commands.application.slash.autocomplete;

import com.freya02.botcommands.internal.utils.StringUtils;
import info.debatty.java.stringsimilarity.NGram;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Class which contains the algorithms used by the autocomplete handlers
 * <br>You might use them if for example you want to provide your own list of choices directly, while still having some sort of relevance sorting
 */
public class AutocompleteAlgorithms {
	private static final NGram TRI_GRAM = new NGram(3);
	private static final NGram BI_GRAM = new NGram(2);

	/**
	 * Sorts the item with fuzzing matching, the {@value OptionData#MAX_CHOICES} most relevant results should appear at the top
	 * <br>Additionally this removes items which do not start with the query string
	 *
	 * @param items            The items to sort
	 * @param toStringFunction The function to transform an item into a String
	 * @param query            The query to match items against
	 * @param <T>              Type of the items
	 *
	 * @return A list of extract results with the scores of each item
	 */
	public static <T> List<FuzzyResult<T>> fuzzyMatchingWithContinuity(Collection<T> items, ToStringFunction<T> toStringFunction, String query) {
		final List<FuzzyResult<T>> results = new ArrayList<>();
		for (T o : items) {
			final String string = toStringFunction.toString(o);
			if (!StringUtils.startsWithIgnoreCase(string, query)) continue;

			double distance = getDistance(query, string);
			if ((1 - distance) <= 0.1) continue;

			results.add(new FuzzyResult<>(o, string, distance));
		}
		return results.stream().sorted().limit(25).toList();
	}

	private static double getDistance(String query, String string) {
		double distance;
		if (query.length() == 2 || string.length() == 2) {
			distance = BI_GRAM.distance(query, string);
		} else {
			distance = TRI_GRAM.distance(query, string);
		}
		return distance;
	}

	/**
	 * Sorts the item with fuzzing matching, the {@value OptionData#MAX_CHOICES} most relevant results should appear at the top
	 * <br>This algorithm is the same as {@link #fuzzyMatchingWithContinuity(Collection, ToStringFunction, CommandAutoCompleteInteractionEvent)}, except it doesn't check for a prefix
	 *
	 * @param items            The items to sort
	 * @param toStringFunction The function to transform an item into a String
	 * @param query            The query to match items against
	 * @param <T>              Type of the items
	 *
	 * @return A list of extract results with the scores of each item
	 */
	public static <T> List<FuzzyResult<T>> fuzzyMatching(Collection<T> items, ToStringFunction<T> toStringFunction, String query) {
		final List<FuzzyResult<T>> results = new ArrayList<>();
		for (T o : items) {
			final String string = toStringFunction.toString(o);
			double distance = getDistance(query, string);
			if ((1 - distance) <= 0.1) continue;

			results.add(new FuzzyResult<>(o, string, distance));
		}
		return results.stream().sorted().limit(25).toList();
	}
}
