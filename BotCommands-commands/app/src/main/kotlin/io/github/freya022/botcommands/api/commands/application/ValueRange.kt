package io.github.freya022.botcommands.api.commands.application

import net.dv8tion.jda.api.interactions.commands.build.OptionData

class ValueRange private constructor(val min: Number, val max: Number) {

    init {
        require(min.toDouble() in OptionData.MIN_NEGATIVE_NUMBER..OptionData.MAX_POSITIVE_NUMBER) {
            "Minimum value must be between ${OptionData.MIN_NEGATIVE_NUMBER} and ${OptionData.MAX_POSITIVE_NUMBER}, provided: $min)"
        }
        require(max.toDouble() in OptionData.MIN_NEGATIVE_NUMBER..OptionData.MAX_POSITIVE_NUMBER) {
            "Maximum value must be between ${OptionData.MIN_NEGATIVE_NUMBER} and ${OptionData.MAX_POSITIVE_NUMBER}, provided: $max)"
        }
    }

    companion object {
        @JvmStatic
        fun ofLong(minValue: Long, maxValue: Long) =
            ValueRange(minValue, maxValue)

        @JvmStatic
        fun ofDouble(minValue: Double, maxValue: Double) =
            ValueRange(minValue, maxValue)

        @Deprecated("Prefer using the 'valueRange' function of the option builder")
        @JvmSynthetic
        infix fun Long.range(maxValue: Long) = ValueRange(this, maxValue)

        @Deprecated("Prefer using the 'valueRange' function of the option builder")
        @JvmSynthetic
        infix fun Int.range(maxValue: Int) = ValueRange(this, maxValue)

        @Deprecated("Prefer using the 'valueRange' function of the option builder")
        @JvmSynthetic
        infix fun Double.range(maxValue: Double) = ValueRange(this, maxValue)
    }
}
