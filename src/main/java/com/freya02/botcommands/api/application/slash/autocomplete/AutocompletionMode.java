package com.freya02.botcommands.api.application.slash.autocomplete;

import me.xdrop.fuzzywuzzy.Applicable;
import me.xdrop.fuzzywuzzy.FuzzySearch;

import java.util.Collection;

public enum AutocompletionMode {
	/**
	 * Sorts the strings by fuzzy search score
	 * <br>This shows the most relevant results most of the time, regardless of if the user did a few mistakes when typing
	 *
	 * @see FuzzySearch#extractTop(String, Collection, Applicable, int)
	 */
	FUZZY,

	/**
	 * Sorts the strings by the smallest string that also starts with the user query
	 * <br>Might be useful for when exact names are needed
	 */
	CONTINUITY
}
