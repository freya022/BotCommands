package com.freya02.botcommands.api.parameters

import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.internal.commands.prefixed.TextCommandVariation
import com.freya02.botcommands.internal.utils.throwUser
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import java.util.regex.Pattern

/**
 * Interface which indicates this class can resolve parameters for regex commands
 */
interface RegexParameterResolver<T : ParameterResolver<T, R>, R> {
    /**
     * Returns a resolved object from this text command interaction
     *
     * @param context   The [BContext] of this bot
     * @param variation The text command variation of the command being executed
     * @param event     The event of the received message
     * @param args      The text arguments of this command, extracted with [pattern]
     *
     * @return The resolved option mapping
     */
    fun resolve(
        context: BContext,
        variation: TextCommandVariation,
        event: MessageReceivedEvent,
        args: Array<String?>
    ): R? = throwUser("${this.javaClass.simpleName} must implement the 'resolve' or 'resolveSuspend' method")

    @JvmSynthetic
    suspend fun resolveSuspend(
        context: BContext,
        variation: TextCommandVariation,
        event: MessageReceivedEvent,
        args: Array<String?>
    ) = resolve(context, variation, event, args)

    /**
     * Returns the pattern required to recognize this parameter
     *
     * This is used to compose a larger pattern which will represent an entire command syntax
     *
     * If you wish to apply flags to this pattern, please enable them inside the regular expression instead of on the pattern,
     * as the pattern aggregator will not take any flags into account.
     *
     * You can enable regex flags using the `(?[flags])` notation,
     * such as `(?i)` to enable case-insensitive matching, and `(?-i)` to disable it.
     *
     * Make sure to disable your modifiers when you are done using them, as they could affect other patterns.
     *
     * You can find more information about regex modifiers [here](https://www.regular-expressions.info/modifiers.html)
     *
     * @return The [Pattern] for this parameter
     */
    val pattern: Pattern

    /**
     * Returns an example string for this parameter
     *
     * This is only used to construct an example command internally and test the whole command's regex against it
     *
     * If the regex does not match the constructed example command, the framework will throw as the regex is deemed "too complex"
     *
     * @return An example string for validation purposes
     */
    val testExample: String

    val preferredPattern: Pattern
        get() = when (this) {
            is QuotableRegexParameterResolver -> this.quotedPattern
            else -> pattern
        }

    fun getHelpExample(isID: Boolean): String
}