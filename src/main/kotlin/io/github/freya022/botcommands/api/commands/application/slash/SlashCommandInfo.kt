package io.github.freya022.botcommands.api.commands.application.slash

import io.github.freya022.botcommands.api.commands.application.ApplicationCommandInfo
import io.github.freya022.botcommands.api.commands.application.slash.options.SlashCommandOption
import io.github.freya022.botcommands.api.commands.application.slash.options.SlashCommandParameter

/**
 * Represents a slash command, can be [top-level][TopLevelSlashCommandInfo] or a [subcommand][SlashSubcommandInfo]
 */
interface SlashCommandInfo : ApplicationCommandInfo {
    override val topLevelInstance: TopLevelSlashCommandInfo

    /**
     * The description of this slash command.
     *
     * May have been set manually or come from a **root** localization bundle.
     */
    val description: String

    override val parameters: List<SlashCommandParameter>

    override val discordOptions: List<SlashCommandOption>
        get() = parameters.flatMap { it.allOptions }.filterIsInstance<SlashCommandOption>()

    /**
     * Mention for this slash command.
     *
     * Equivalent to `"</$fullCommandName:${topLevelInstance.id}>"`.
     */
    val asMention: String get() = "</$fullCommandName:${topLevelInstance.id}>"

    @Suppress("DeprecatedCallableAddReplaceWith")
    @Deprecated("For removal, confusing on whether it searches nested parameters, prefer using collection operations on 'parameters' instead, make an extension or an utility method")
    override fun getParameter(declaredName: String): SlashCommandParameter? =
        parameters.find { it.name == declaredName }

    /**
     * Returns the option with the supplied *display name* (i.e., the name you see on Discord),
     * or `null` if not found.
     */
    fun getOptionByDisplayName(name: String): SlashCommandOption? =
        discordOptions.find { it.discordName == name }
}