package com.freya02.botcommands.api.commands.application

import net.dv8tion.jda.api.interactions.commands.build.OptionData

class LengthRange private constructor(val min: Int, val max: Int) {
    init {
        if (min <= 0) {
            throw IllegalArgumentException("String length must be positive")
        } else if (max > OptionData.MAX_STRING_OPTION_LENGTH) {
            throw IllegalArgumentException("Cannot have a string length higher than ${OptionData.MAX_STRING_OPTION_LENGTH}")
        }
    }

    companion object {
        @JvmStatic
        fun of(min: Int, max: Int) = LengthRange(min, max)

        infix fun Int.range(maxLength: Int) = LengthRange(this, maxLength)
    }
}