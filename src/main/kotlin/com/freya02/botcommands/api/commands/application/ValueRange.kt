package com.freya02.botcommands.api.commands.application

class ValueRange internal constructor(val min: Number, val max: Number) {
    companion object {
        @JvmStatic
        fun ofLong(minValue: Long, maxValue: Long) =
            ValueRange(minValue, maxValue)

        @JvmStatic
        fun ofDouble(minValue: Double, maxValue: Double) =
            ValueRange(minValue, maxValue)

        infix fun Long.range(maxValue: Long) = ValueRange(this, maxValue)

        infix fun Int.range(maxValue: Int) = ValueRange(this, maxValue)

        infix fun Double.range(maxValue: Double) = ValueRange(this, maxValue)
    }
}