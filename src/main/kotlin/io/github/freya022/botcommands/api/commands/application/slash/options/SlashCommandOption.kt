package io.github.freya022.botcommands.api.commands.application.slash.options

import io.github.freya022.botcommands.api.commands.application.LengthRange
import io.github.freya022.botcommands.api.commands.application.ValueRange
import io.github.freya022.botcommands.api.commands.application.options.ApplicationCommandOption
import io.github.freya022.botcommands.api.commands.application.slash.SlashCommandInfo
import io.github.freya022.botcommands.api.parameters.resolvers.SlashParameterResolver
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.OptionType

/**
 * Represents a Discord input option of a slash command.
 */
interface SlashCommandOption : ApplicationCommandOption {

    @Deprecated("Renamed to 'executable'", replaceWith = ReplaceWith("executable"))
    override val command get() = executable
    override val executable: SlashCommandInfo

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