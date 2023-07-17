package com.freya02.botcommands.api.commands.application.slash.annotations

import com.freya02.botcommands.api.commands.application.slash.builder.SlashCommandOptionBuilder
import net.dv8tion.jda.api.interactions.commands.build.OptionData

/**
 * Sets the minimum and maximum string length on the specified [SlashOption].
 *
 * **Note:** this is only for string types!
 *
 * @see SlashCommandOptionBuilder.lengthRange DSL equivalent
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class Length(
    /**
     * The minimum value of this parameter (included)
     */
    val min: Int = 1,

    /**
     * The maximum value of this parameter (included)
     */
    val max: Int = OptionData.MAX_STRING_OPTION_LENGTH
)
