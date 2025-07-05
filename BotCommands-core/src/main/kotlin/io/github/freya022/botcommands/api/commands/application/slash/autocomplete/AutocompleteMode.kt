package io.github.freya022.botcommands.api.commands.application.slash.autocomplete

/**
 * See values
 */
enum class AutocompleteMode {
    /**
     * Sorts the strings by fuzzy search score
     *
     * This shows the most relevant results most of the time, regardless of if the user did a few mistakes when typing
     *
     * @see AutocompleteAlgorithms.fuzzyMatching
     */
    FUZZY,

    /**
     * Sorts the strings by the smallest string that also starts with the user query
     *
     * Might be useful for when exact names are needed
     *
     * @see AutocompleteAlgorithms.fuzzyMatchingWithContinuity
     */
    CONTINUITY
}
