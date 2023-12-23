package io.github.freya022.botcommands.internal.commands.prefixed

import info.debatty.java.stringsimilarity.NormalizedLevenshtein
import io.github.freya022.botcommands.api.commands.text.TextSuggestionSupplier
import kotlin.math.min

internal object DefaultTextSuggestionSupplier : TextSuggestionSupplier {
    private val normalizedLevenshtein = NormalizedLevenshtein()

    override fun getSuggestions(topLevelName: String, candidates: List<TopLevelTextCommandInfo>): Collection<TopLevelTextCommandInfo> {
        // Keep strings that have more than 90% matching on same-length strings
        val partialMatches = candidates.filter { normalizedLevenshtein.partialSimilarity(it.name, topLevelName) > 0.9 }

        val matches = buildList {
            partialMatches.forEach {
                val similarity = normalizedLevenshtein.similarity(it.name, topLevelName)
                if (similarity > 0.42) {
                    this += it to (1 - similarity)
                }
            }
        }

        return matches.sortedBy { it.second }.map { it.first }
    }

    // https://stackoverflow.com/a/53756045
    // Similarity between the substring of the shortest length
    private fun NormalizedLevenshtein.partialSimilarity(s1: String, s2: String): Double {
        val minLength = min(s1.length, s2.length)
        val cutS1 = s1.take(minLength)
        val cutS2 = s2.take(minLength)

        return similarity(cutS1, cutS2)
    }
}