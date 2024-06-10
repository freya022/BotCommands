package io.github.freya022.botcommands.api.commands.text

import info.debatty.java.stringsimilarity.NormalizedLevenshtein
import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService

/**
 * Interface to supply text command suggestions from a top level name.
 *
 * ### Default implementation
 *
 * The default implementation uses a [normalized Levenshtein][NormalizedLevenshtein] similarity and does the following:
 * - Get the partial similarity between all commands, keeping 90% similarity and above (partial similarity compares the shortest substring of both the command name and the user input)
 * - Get the similarity of the remaining commands, keeping 42% similarity and above
 *
 * @see InterfacedService @InterfacedService
 */
@InterfacedService(acceptMultiple = false)
fun interface TextSuggestionSupplier {
    /**
     * Computes command suggestions from the [user input][topLevelName] and the [available commands][candidates].
     *
     * Commands are already filtered to only include those which can be shown,
     * only requiring you to match against the user input.
     *
     * @param topLevelName The user input, which should correspond to the top level command
     * @param candidates   The available commands, all of them can be shown to the user
     *
     * @return The list of commands suggested to the user
     */
    fun getSuggestions(topLevelName: String, candidates: List<TopLevelTextCommandInfo>): Collection<TopLevelTextCommandInfo>
}