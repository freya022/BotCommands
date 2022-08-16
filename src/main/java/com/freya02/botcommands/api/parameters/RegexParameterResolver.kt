package com.freya02.botcommands.api.parameters

import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.internal.prefixed.TextCommandInfo
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import java.util.regex.Pattern

/**
 * Interface which indicates this class can resolve parameters for regex commands
 */
interface RegexParameterResolver {
    /**
     * Returns a resolved object from this text command interaction
     *
     * @param context The [BContext] of this bot
     * @param info    The text command info of the command being executed
     * @param event   The event of this received message
     * @param args    The text arguments of this command, extracted with [pattern]
     * @return The resolved option mapping
     */
    fun resolve(
        context: BContext,
        info: TextCommandInfo,
        event: MessageReceivedEvent,
        args: Array<String?>
    ): Any? = TODO("${this.javaClass.simpleName} must implement the 'resolve' or 'resolveSuspend' method")

    @JvmSynthetic
    suspend fun resolveSuspend(
        context: BContext,
        info: TextCommandInfo,
        event: MessageReceivedEvent,
        args: Array<String?>
    ) = resolve(context, info, event, args)

    /**
     * Returns the pattern required to recognize this parameter
     *
     * This is used to compose a larger pattern which will represent an entire command syntax
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
}