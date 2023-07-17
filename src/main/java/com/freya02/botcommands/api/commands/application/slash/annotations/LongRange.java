package com.freya02.botcommands.api.commands.application.slash.annotations

import com.freya02.botcommands.api.commands.application.slash.builder.SlashCommandOptionBuilder;

/**
 * Sets the minimum and maximum values on the specified [SlashOption].
 *
 * **Note:** this is only for integer types!
 *
 * @see SlashCommandOptionBuilder.valueRange DSL equivalent
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class LongRange(
    /**
     * The minimum value of this parameter (included)
     */
    val from: Long,

    /**
     * The maximum value of this parameter (included)
     */
    val to: Long
)
