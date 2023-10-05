package io.github.freya022.botcommands.api.commands.application.slash.annotations

import io.github.freya022.botcommands.api.commands.application.slash.builder.SlashCommandOptionBuilder

/**
 * Sets the minimum and maximum values on the specified [@SlashOption][SlashOption].
 *
 * **Note:** This is only for floating point number types!
 *
 * @see SlashCommandOptionBuilder.valueRange DSL equivalent
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class DoubleRange(
    /**
     * The minimum value of this parameter (included)
     */
    val from: Double,

    /**
     * The maximum value of this parameter (included)
     */
    val to: Double
)
