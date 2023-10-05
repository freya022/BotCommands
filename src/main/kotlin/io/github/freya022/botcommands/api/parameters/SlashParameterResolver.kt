package io.github.freya022.botcommands.api.parameters

import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption
import io.github.freya022.botcommands.api.commands.application.slash.builder.SlashCommandOptionBuilder
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.internal.commands.application.slash.SlashCommandInfo
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command.Choice
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType

/**
 * Interface which indicates this class can resolve parameters for application commands
 */
interface SlashParameterResolver<T, R> where T : ParameterResolver<T, R>,
                                             T : SlashParameterResolver<T, R> {
    /**
     * Returns the supported [OptionType] for this slash command parameter
     *
     * @return The supported [OptionType] for this slash command parameter
     */
    val optionType: OptionType

    /**
     * Returns a constant list of [choices][Choice] for this slash parameter resolver
     *
     * This will be applied to all command parameters of this type, but can still be overridden if there are choices set in [ApplicationCommand.getOptionChoices]
     *
     * This could be useful for, say, an enum resolver, or anything where the choices do not change between commands
     *
     * **Note:** This requires enabling [SlashOption.usePredefinedChoices] (annotation-declared) / [SlashCommandOptionBuilder.usePredefinedChoices] (DSL-declared).
     */
    fun getPredefinedChoices(guild: Guild?): Collection<Choice> {
        return emptyList()
    }

    /**
     * Returns a resolved object for this [OptionMapping].
     *
     * **Note:** If the value is not resolvable, the bot should reply with an error message.
     *
     * @param context       The [BContext] of this bot
     * @param info          The slash command info of the command being executed
     * @param event         The event of this interaction, could be a [SlashCommandInteractionEvent] or a [CommandAutoCompleteInteractionEvent]
     * @param optionMapping The [OptionMapping] to be resolved
     * @return The resolved option mapping
     */
    fun resolve(context: BContext, info: SlashCommandInfo, event: CommandInteractionPayload, optionMapping: OptionMapping): R? =
        throw NotImplementedError("${this.javaClass.simpleName} must implement the 'resolve' or 'resolveSuspend' method")

    @JvmSynthetic
    suspend fun resolveSuspend(context: BContext, info: SlashCommandInfo, event: CommandInteractionPayload, optionMapping: OptionMapping) =
        resolve(context, info, event, optionMapping)
}
