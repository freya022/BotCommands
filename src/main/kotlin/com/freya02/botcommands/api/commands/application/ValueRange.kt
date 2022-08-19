package com.freya02.botcommands.api.commands.application

class ValueRange internal constructor(val min: Number, val max: Number) {
    companion object {
        @JvmStatic
        fun ofLong(minValue: Long, maxValue: Long) =
            com.freya02.botcommands.api.commands.application.ValueRange(minValue, maxValue)

        @JvmStatic
        fun ofDouble(minValue: Double, maxValue: Double) =
            com.freya02.botcommands.api.commands.application.ValueRange(minValue, maxValue)

        infix fun Long.range(maxValue: Long) = com.freya02.botcommands.api.commands.application.ValueRange(this, maxValue)

        infix fun Int.range(maxValue: Int) = com.freya02.botcommands.api.commands.application.ValueRange(this, maxValue)

        infix fun Double.range(maxValue: Double) = com.freya02.botcommands.api.commands.application.ValueRange(this, maxValue)
    }
}