package com.freya02.botcommands.api.commands.application.slash.autocomplete;

import java.util.Collection;

/**
 * See values
 */
public enum AutocompleteMode {
	/**
	 * Sorts the strings by fuzzy search score
	 * <br>This shows the most relevant results most of the time, regardless of if the user did a few mistakes when typing
	 *
	 * @see AutocompleteAlgorithms#fuzzyMatching(Collection, ToStringFunction, String)
	 */
	FUZZY,

	/**
	 * Sorts the strings by the smallest string that also starts with the user query
	 * <br>Might be useful for when exact names are needed
	 *
	 * @see AutocompleteAlgorithms#fuzzyMatchingWithContinuity(Collection, ToStringFunction, String)
	 */
	CONTINUITY
}
