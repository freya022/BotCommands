package io.github.freya022.botcommands.api.commands.annotations

import io.github.freya022.botcommands.api.commands.application.slash.annotations.MentionsString
import net.dv8tion.jda.api.interactions.commands.build.CommandData

/**
 * Generates N command options from the specified command **input** option.
 *
 * The target parameter must be of type [List].
 *
 * You can configure how many arguments are required with [numRequired].
 *
 * See [@MentionsString][MentionsString] for a way to get a list of mentionable (user/member/role/channel...)
 * without a vararg, using a single string.
 *
 * @see MentionsString @MentionsString
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class VarArgs(
    /**
     * The number of times this option needs to appear, must be positive.
     */
    val value: Int,

    /**
     * The number of required options for this vararg.
     *
     * For slash commands, this must can be 0, positive, or how many remaining options there are until [MAX_OPTIONS][CommandData.MAX_OPTIONS].
     *
     * For text commands, this must be at least 1.
     */
    val numRequired: Int = 1
)
