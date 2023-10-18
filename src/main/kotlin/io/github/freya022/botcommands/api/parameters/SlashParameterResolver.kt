package io.github.freya022.botcommands.api.parameters

import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption
import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.annotations.AutocompleteHandler
import io.github.freya022.botcommands.api.commands.application.slash.builder.SlashCommandOptionBuilder
import io.github.freya022.botcommands.api.localization.DefaultMessages
import io.github.freya022.botcommands.internal.commands.application.slash.SlashCommandInfo
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command.Choice
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType
import kotlin.reflect.KParameter
import kotlin.reflect.KType

/**
 * Parameter resolver for parameters of [@JDASlashCommand][JDASlashCommand] and [@AutocompleteHandler][AutocompleteHandler].
 */
interface SlashParameterResolver<T, R : Any> where T : ParameterResolver<T, R>,
                                                   T : SlashParameterResolver<T, R> {
    /**
     * Returns the corresponding [OptionType] for this slash command parameter.
     */
    val optionType: OptionType

    /**
     * Returns a constant list of [choices][Choice] for this slash parameter resolver.
     *
     * This will be applied to all command parameters of this type,
     * but can still be overridden if there are choices set in [ApplicationCommand.getOptionChoices].
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
     * or [optional][KParameter.isOptional], and the resolver hasn't replied to this interaction,
     * then the slash command aborts with an [unresolvable option error message][DefaultMessages.getSlashCommandUnresolvableOptionMsg].
     *
     * Meaning that if the value is not resolvable,
     * the bot should reply with its own message to avoid the generic error message.
     *
     * @param info          The data of the command being executed
     * @param event         The corresponding event, could be a [SlashCommandInteractionEvent] or a [CommandAutoCompleteInteractionEvent]
     * @param optionMapping The [OptionMapping] to be resolved
     */
    fun resolve(info: SlashCommandInfo, event: CommandInteractionPayload, optionMapping: OptionMapping): R? =
        throw NotImplementedError("${this.javaClass.simpleName} must implement the 'resolve' or 'resolveSuspend' method")

    /**
     * Returns a resolved object for this [OptionMapping].
     *
     * If this returns `null`, and the parameter is required, i.e., not [nullable][KType.isMarkedNullable]
     * or [optional][KParameter.isOptional], and the resolver hasn't replied to this interaction,
     * then the slash command aborts with an [unresolvable option error message][DefaultMessages.getSlashCommandUnresolvableOptionMsg].
     *
     * Meaning that if the value is not resolvable,
     * the bot should reply with its own message to avoid the generic error message.
     *
     * @param info          The data of the command being executed
     * @param event         The corresponding event, could be a [SlashCommandInteractionEvent] or a [CommandAutoCompleteInteractionEvent]
     * @param optionMapping The [OptionMapping] to be resolved
     */
    @JvmSynthetic
    suspend fun resolveSuspend(info: SlashCommandInfo, event: CommandInteractionPayload, optionMapping: OptionMapping) =
        resolve(info, event, optionMapping)
}
