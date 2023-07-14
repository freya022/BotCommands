package com.freya02.botcommands.api.commands.application.slash.autocomplete

import info.debatty.java.stringsimilarity.NGram
import net.dv8tion.jda.api.interactions.commands.build.OptionData

object AutocompleteAlgorithms {
    private val biGram = NGram(2)
    private val triGram = NGram(3)

    /**
     * Sorts the item with fuzzing matching, the [OptionData.MAX_CHOICES] most relevant results should appear at the top
     *
     * This algorithm is the same as [fuzzyMatchingWithContinuity], except it doesn't check for a prefix
     *
     * @param items            The items to sort
     * @param toStringFunction The function to transform an item into a String
     * @param query            The query to match items against
     *
     * @return A collection of extract results with the scores of each item
     */
    @JvmStatic
    fun <T> fuzzyMatching(items: Collection<T>, toStringFunction: ToStringFunction<T>, query: String): Collection<FuzzyResult<T>> {
        val list = sortedSetOf<FuzzyResult<T>>()
        items.forEach {
            val str = toStringFunction.toString(it)
            val algo = when {
                str.length < 3 || query.length < 3 -> biGram
                else -> triGram
            }

            val distance = algo.distance(str, query)
            list += FuzzyResult(it, str, distance)
        }

        return list
    }

    /**
     * Sorts the item with fuzzing matching, the [OptionData.MAX_CHOICES] most relevant results should appear at the top
     *
     * Additionally, this removes items which do not start with the query string
     *
     * @param items            The items to sort
     * @param toStringFunction The function to transform an item into a String
     * @param query            The query to match items against
     * @param <T>              Type of the items
     *
     * @return A collection of extract results with the scores of each item
     */
    @JvmStatic
    fun <T> fuzzyMatchingWithContinuity(
        items: Collection<T>,
        toStringFunction: ToStringFunction<T>,
        query: String
    ): Collection<FuzzyResult<T>> {
        val list = sortedSetOf<FuzzyResult<T>>()
        items.forEach {
            val str = toStringFunction.toString(it)
            if (!str.startsWith(query, ignoreCase = true)) return@forEach

            val algo = when {
                str.length < 3 || query.length < 3 -> biGram
                else -> triGram
            }

            val distance = algo.distance(str, query)
            list += FuzzyResult(it, str, distance)
        }

        return list
    }
}