package io.github.freya022.botcommands.api.commands.application.slash.annotations

import io.github.freya022.botcommands.api.commands.application.slash.options.builder.SlashCommandOptionBuilder
import net.dv8tion.jda.api.interactions.commands.build.OptionData

/**
 * Sets the minimum and maximum values on the specified [@SlashOption][SlashOption].
 * Must be between [OptionData.MIN_NEGATIVE_NUMBER] and [OptionData.MAX_POSITIVE_NUMBER].
 *
 * **Note:** This is only for integer types!
 *
 * @see SlashCommandOptionBuilder.valueRange DSL equivalent
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
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
