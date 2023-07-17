package com.freya02.botcommands.api.commands.application.slash.annotations

import com.freya02.botcommands.api.commands.application.slash.builder.SlashCommandBuilder
import com.freya02.botcommands.api.commands.prefixed.annotations.TextOption

/**
 * Generates N command options from the specified [SlashOption] or [TextOption].
 *
 * The target parameter must be of type [List].
 *
 * You can configure how many arguments are required with [numRequired].
 *
 * **Note:** you are limited to 1 vararg parameter in text commands.
 *
 * @see SlashCommandBuilder.optionVararg
 * @see SlashCommandBuilder.inlineClassOptionVararg
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class VarArgs(
    /**
     * The number of times this option needs to appear, which must be between 1 and {@value CommandData#MAX_OPTIONS}.
     */
    val value: Int,

    /**
     * The number of required options for this vararg.
     */
    val numRequired: Int = 1
)
