package io.github.freya022.botcommands.api.parameters.resolvers

import io.github.freya022.botcommands.api.commands.application.ApplicationCommandResolverData
import io.github.freya022.botcommands.api.commands.application.SlashOptionChoiceProvider
import io.github.freya022.botcommands.api.commands.application.messages.ApplicationCommandsMessages
import io.github.freya022.botcommands.api.commands.application.slash.annotations.ChannelTypes
import io.github.freya022.botcommands.api.commands.application.slash.annotations.MentionsString
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption
import io.github.freya022.botcommands.api.commands.application.slash.options.SlashCommandOption
import io.github.freya022.botcommands.api.commands.application.slash.options.builder.SlashCommandOptionBuilder
import io.github.freya022.botcommands.api.core.entities.InputUser
import io.github.freya022.botcommands.api.parameters.ParameterResolver
import io.github.freya022.botcommands.api.parameters.ResolverRequest
import io.github.freya022.botcommands.internal.parameters.resolvers.ResolverMarker
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command.Choice
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType
import kotlin.reflect.KParameter
import kotlin.reflect.KType

/**
 * Resolver for slash command options, including referenced autocomplete parameters.
 *
 * Needs to be implemented alongside a [ParameterResolver] subclass.
 *
 * A [ApplicationCommandResolverData] is passed in the [ResolverRequest] from resolver factories.
 *
 * ### Types supported by default
 * - [String]
 * - [Boolean]
 * - [Int]
 * - [Long]
 * - [Double]
 * - [Emoji]
 * - [IMentionable] (only when mentioned)
 * - [List] of mentionable (see [@MentionsString][MentionsString])
 * - [Role]
 * - [UserSnowflake]
 * - [User]
 * - [Member]
 * - [InputUser]
 * - [GuildChannel] subtypes, the channel types are set automatically depending on the type,
 * but a broader channel type can be used and restricted to multiple concrete types by using [@ChannelTypes][ChannelTypes]
 * - [Guild] (input as a string)
 * - [Message.Attachment]
 *
 * @param T Type of the implementation
 * @param R Type of the returned resolved objects
 */
interface SlashParameterResolver<T, R : Any> : IParameterResolver<T>, ResolverMarker
        where T : ParameterResolver<T, R>,
              T : SlashParameterResolver<T, R> {

    /**
     * Returns the corresponding [OptionType] for this slash command parameter.
     */
    val optionType: OptionType

    /**
     * Returns a constant list of [choices][Choice] for this slash parameter resolver.
     *
     * This will be applied to all command parameters of this type,
     * but can still be overridden if there are choices set by [SlashOptionChoiceProvider].
     *
     * This could be useful for, say, an enum resolver, or anything where the choices do not change between commands.
     *
     * **Note:** This requires enabling [SlashOption.usePredefinedChoices] (annotation-declared) / [SlashCommandOptionBuilder.usePredefinedChoices] (DSL-declared).
     */
    fun getPredefinedChoices(guild: Guild?): Collection<Choice> {
        return emptyList()
    }

    /**
     * Returns a resolved object for this [OptionMapping].
     *
     * If this returns `null`, and the parameter is required, i.e., not [nullable][KType.isMarkedNullable]
     * or [optional][KParameter.isOptional], then the interaction is ignored,
     * and you should reply if this is a [SlashCommandInteractionEvent].
     *
     * If the interaction is not replied to,
     * the handler sends an [unresolvable option error message][ApplicationCommandsMessages.slashCommandUnresolvableOption].
     *
     * @param option        The option currently being resolved
     * @param event         The corresponding event, could be a [SlashCommandInteractionEvent] or a [CommandAutoCompleteInteractionEvent]
     * @param optionMapping The [OptionMapping] to be resolved
     */
    fun resolve(option: SlashCommandOption, event: CommandInteractionPayload, optionMapping: OptionMapping): R? =
        throw NotImplementedError("${this.javaClass.simpleName} must implement the 'resolve' or 'resolveSuspend' method")

    /**
     * Returns a resolved object for this [OptionMapping].
     *
     * If this returns `null`, and the parameter is required, i.e., not [nullable][KType.isMarkedNullable]
     * or [optional][KParameter.isOptional], then the interaction is ignored,
     * and you should reply if this is a [SlashCommandInteractionEvent].
     *
     * If the interaction is not replied to,
     * the handler sends an [unresolvable option error message][ApplicationCommandsMessages.slashCommandUnresolvableOption].
     *
     * @param option        The option currently being resolved
     * @param event         The corresponding event, could be a [SlashCommandInteractionEvent] or a [CommandAutoCompleteInteractionEvent]
     * @param optionMapping The [OptionMapping] to be resolved
     */
    @JvmSynthetic
    suspend fun resolveSuspend(option: SlashCommandOption, event: CommandInteractionPayload, optionMapping: OptionMapping) =
        resolve(option, event, optionMapping)
}
