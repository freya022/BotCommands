package io.github.freya022.botcommands.api.pagination.menu

import kotlin.math.floor
import kotlin.math.log10

fun interface RowPrefixSupplier {
    fun apply(entryNum: Int, maxEntries: Int): String

    companion object {
        @JvmStatic
        val paddedNumberPrefix = RowPrefixSupplier { entryNum: Int, maxEntry: Int ->
            val spaces = getPadding(entryNum, maxEntry)
            "`" + " ".repeat(spaces) + entryNum + ".` "
        }

        /**
         * Returns the padding needed between this entry number and the maximum entry number
         *
         * @param entryNum The current entry number
         * @param maxEntry The maximum entry number
         *
         * @return The number of padding spaces needed
         */
        @JvmStatic
        fun getPadding(entryNum: Int, maxEntry: Int): Int {
            require(entryNum > 0)

            val entryDigits = floor(log10(entryNum.toDouble()) + 1)
            val maxEntryDigits = floor(log10(maxEntry.toDouble()) + 1)
            return (maxEntryDigits - entryDigits).toInt()
        }
    }
}
