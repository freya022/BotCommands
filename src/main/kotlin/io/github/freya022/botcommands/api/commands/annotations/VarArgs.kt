package io.github.freya022.botcommands.api.commands.annotations

import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption
import io.github.freya022.botcommands.api.commands.application.slash.builder.SlashCommandBuilder
import io.github.freya022.botcommands.api.commands.text.annotations.TextOption
import io.github.freya022.botcommands.api.commands.text.builder.TextCommandVariationBuilder

/**
 * Generates N command options from the specified [@SlashOption][SlashOption] or [@TextOption][TextOption].
 *
 * The target parameter must be of type [List].
 *
 * You can configure how many arguments are required with [numRequired].
 *
 * See [@MentionsString][MentionsString] for a way to get a list of mentionable (user/member/role/channel...)
 * without a vararg, using a single string.
 *
 * **Note:** You are limited to one vararg parameter in text commands.
 *
 * @see TextCommandVariationBuilder.optionVararg
 * @see TextCommandVariationBuilder.inlineClassOptionVararg
 *
 * @see SlashCommandBuilder.optionVararg
 * @see SlashCommandBuilder.inlineClassOptionVararg
 *
 * @see MentionsString @MentionsString
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
