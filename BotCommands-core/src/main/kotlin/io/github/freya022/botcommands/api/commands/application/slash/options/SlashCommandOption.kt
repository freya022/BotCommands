package io.github.freya022.botcommands.api.commands.application.slash.options

import io.github.freya022.botcommands.api.commands.application.LengthRange
import io.github.freya022.botcommands.api.commands.application.ValueRange
import io.github.freya022.botcommands.api.commands.application.options.ApplicationCommandOption
import io.github.freya022.botcommands.api.parameters.resolvers.SlashParameterResolver
import net.dv8tion.jda.api.interactions.FileType
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.OptionType

/**
 * Represents a Discord input option of a slash command.
 */
interface SlashCommandOption : ApplicationCommandOption {

    override val executable get() = parent.executable
    override val parent: SlashCommandParameter

    /**
     * The name of this option as shown on Discord.
     */
    val discordName: String

    /**
     * The description of this option.
     *
     * May have been set manually or come from a **root** localization bundle.
     */
    val description: String

    /**
     * Whether this option uses choices given by [SlashParameterResolver.getPredefinedChoices].
     *
     * This property is `false` is [choices] are set.
     */
    val usePredefinedChoices: Boolean

    /**
     * The choices manually set on this option.
     */
    val choices: List<Command.Choice>?

    /**
     * The allowed number range of this option.
     *
     * Only applies on options which have [OptionType.INTEGER]
     * as their resolver's [option type][SlashParameterResolver.optionType].
     */
    val range: ValueRange?

    /**
     * The allowed length of this option.
     *
     * Only applies on options which have [OptionType.STRING]
     * as their resolver's [option type][SlashParameterResolver.optionType].
     */
    val length: LengthRange?

    /**
     * The file types this [Attachment][net.dv8tion.jda.api.entities.Message.Attachment] option is accepting, if it is one.
     */
    val fileTypes: List<FileType>

    /**
     * Whether this option uses autocomplete
     */
    fun hasAutocomplete(): Boolean

    /**
     * Invalidates this option's autocomplete cache, if configured.
     *
     * @throws IllegalStateException If this option has [no autocomplete][hasAutocomplete]
     */
    fun invalidateAutocomplete()
}
