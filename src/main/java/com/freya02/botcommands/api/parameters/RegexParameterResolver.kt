package com.freya02.botcommands.api.parameters

import com.freya02.botcommands.annotations.api.annotations.Optional
import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.internal.prefixed.TextCommandInfo
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import java.util.regex.Pattern

/**
 * Interface which indicates this class can resolve parameters for regex commands
 */
@JvmDefaultWithCompatibility
interface RegexParameterResolver {
    /**
     * Returns a resolved object from this text command interaction
     *
     * @param context The [BContext] of this bot
     * @param info    The text command info of the command being executed
     * @param event   The event of this received message
     * @param args    The text arguments of this command, extracted with [.getPattern]
     * @return The resolved option mapping
     */
    fun resolve(
        context: BContext,
        info: TextCommandInfo,
        event: MessageReceivedEvent,
        args: Array<String?>
    ): Any?

    /**
     * Returns the pattern required to recognize this parameter
     * <br></br>This is used to compose a larger pattern which will represent an entire command syntax
     *
     * @return The [Pattern] for this parameter
     */
    fun getPattern(): Pattern

    /**
     * Returns an example string for this parameter
     * <br></br>This is only used to construct an example command internally and test the whole command's regex against it
     * <br></br>If the regex does not match the constructed example command, the framework will throw as the regex is deemed "too complex", see [Optional]
     *
     * @return An example string for validation purposes
     * @see Optional
     */
    fun getTestExample(): String

    fun getPreferredPattern(): Pattern {
        return if (this is QuotableRegexParameterResolver) {
            (this as QuotableRegexParameterResolver).quotedPattern
        } else {
            getPattern()
        }
    }
}