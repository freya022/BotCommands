package io.github.freya022.botcommands.api.parameters.resolvers

import io.github.freya022.botcommands.api.commands.text.BaseCommandEvent
import io.github.freya022.botcommands.api.commands.text.annotations.JDATextCommandVariation
import io.github.freya022.botcommands.api.commands.text.options.TextCommandOption
import io.github.freya022.botcommands.api.core.entities.InputUser
import io.github.freya022.botcommands.api.parameters.ParameterResolver
import io.github.freya022.botcommands.internal.parameters.resolvers.ResolverMarker
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import java.util.regex.Pattern
import kotlin.reflect.KParameter
import kotlin.reflect.KType

/**
 * Resolver for text command options.
 *
 * Needs to be implemented alongside a [ParameterResolver] subclass.
 *
 * **Note:** You may use [QuotableTextParameterResolver] in some situations to help with content parsing.
 *
 * ### Types supported by default
 * - [String]
 * - [Boolean]
 * - [Int]
 * - [Long]
 * - [Double]
 * - [Emoji]
 * - [IMentionable] (only when mentioned)
 * - [Role]
 * - [UserSnowflake]
 * - [User]
 * - [Member]
 * - [InputUser]
 * - [GuildChannel] subtypes
 * - [Guild]
 *
 * @param T Type of the implementation
 * @param R Type of the returned resolved objects
 */
interface TextParameterResolver<T, R : Any> : IParameterResolver<T>, ResolverMarker
        where T : ParameterResolver<T, R>,
              T : TextParameterResolver<T, R> {

    /**
     * Returns a resolved object from this text command.
     *
     * If this returns `null`, and the parameter is required, i.e., not [nullable][KType.isMarkedNullable]
     * or [optional][KParameter.isOptional], then the handler goes to the next command variation.
     *
     * See the [@JDATextCommandVariation][JDATextCommandVariation] documentation for more details about text command variations.
     *
     * @param option The option currently being resolved
     * @param event  The corresponding event
     * @param args   The arguments of this parameter, extracted with [pattern]
     */
    fun resolve(option: TextCommandOption, event: MessageReceivedEvent, args: Array<String?>): R? =
        throw NotImplementedError("${this.javaClass.simpleName} must implement the 'resolve' or 'resolveSuspend' method")

    /**
     * Returns a resolved object from this text command.
     *
     * If this returns `null`, and the parameter is required, i.e., not [nullable][KType.isMarkedNullable]
     * or [optional][KParameter.isOptional], then the handler goes to the next command variation.
     *
     * See the [@JDATextCommandVariation][JDATextCommandVariation] documentation for more details about text command variations.
     *
     * @param option The option currently being resolved
     * @param event  The corresponding event
     * @param args   The arguments of this parameter, extracted with [pattern]
     */
    @JvmSynthetic
    suspend fun resolveSuspend(option: TextCommandOption, event: MessageReceivedEvent, args: Array<String?>) =
        resolve(option, event, args)

    /**
     * Returns the pattern required to recognize this parameter,
     * used in a larger pattern representing the entire command.
     *
     * ### Pattern flags
     *
     * - Flags added on this pattern will not affect other options
     * - If you use "embedded flag expressions" (e.g. `(?i)`), make sure you close them (`(?-i)`)
     * - Using flags that do not have an embedded flag expression, is not allowed,
     *     you can check if it has one from the flag's documentation
     *
     * [Pattern.UNICODE_CASE] and [Pattern.UNICODE_CHARACTER_CLASS] are enabled by default.
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
        get() = pattern

    /**
     * Returns a help example for the supplied option.
     *
     * **Tip:** You may use the event as a way to get sample data (such as getting the member, channel, roles, etc...).
     *
     * @param option    The option of the command being shown in the help content
     * @param event     The event of the command that triggered help content to be displayed
     */
    fun getHelpExample(option: TextCommandOption, event: BaseCommandEvent): String
}
