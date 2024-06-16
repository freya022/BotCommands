package io.github.freya022.botcommands.api.commands.application.slash

import io.github.freya022.botcommands.api.commands.application.ApplicationCommandInfo

/**
 * Represents a slash command, can be [top-level][TopLevelSlashCommandInfo] or a [subcommand][SlashSubcommandInfo]
 */
interface SlashCommandInfo : ApplicationCommandInfo {
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

    override fun getParameter(declaredName: String): SlashCommandParameter? =
        parameters.find { it.name == declaredName }

    /**
     * Returns the option with the supplied *display name* (i.e., the name you see on Discord),
     * or `null` if not found.
     */
    fun getOptionByDisplayName(name: String): SlashCommandOption? =
        discordOptions.find { it.discordName == name }
}