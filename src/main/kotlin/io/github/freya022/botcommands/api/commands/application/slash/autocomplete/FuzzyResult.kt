package io.github.freya022.botcommands.api.commands.application.slash.autocomplete

@JvmRecord
data class FuzzyResult<T>(val item: T, val string: String, val distance: Double) : Comparable<FuzzyResult<T>> {
    fun similarity(): Double {
        return 1.0 - distance
    }

    //Serves for ordering purpose
    override fun compareTo(other: FuzzyResult<T>): Int {
        if (distance == other.distance) { //This is needed as TreeSet considers entries as duplicated if compare result is 0
            val strCompare = string.compareTo(other.string)
            if (strCompare == 0) {
                return 1 //Don't care about ordering if both strings are equal
            }

            return strCompare
        }

        return distance.compareTo(other.distance)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        val that = other as FuzzyResult<*>

        return item == that.item
    }

    override fun hashCode(): Int {
        return item.hashCode()
    }
}
