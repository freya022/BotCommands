package io.github.freya022.botcommands.api.commands.application.slash

import io.github.freya022.botcommands.api.commands.application.TopLevelApplicationCommandInfo

/**
 * Represents a top-level slash command (i.e. not a subcommand, nor a group).
 *
 * Contains additional info only available on top-level commands.
 */
interface TopLevelSlashCommandInfo : TopLevelApplicationCommandInfo, SlashCommandInfo {
    /**
     * Subcommands of this top-level slash command, the key is the name of the subcommand.
     */
    val subcommands: Map<String, SlashSubcommandInfo>

    /**
     * Subcommand groups of this top-level slash command, the key is the name of the subcommand group.
     */
    val subcommandGroups: Map<String, SlashSubcommandGroupInfo>

    /**
     * Whether this slash command is top-level only.
     *
     * Top-level commands do not contain any subcommands or subcommand groups, and are thus executable.
     */
    val isTopLevelCommandOnly: Boolean
        get() = subcommands.isEmpty() && subcommandGroups.isEmpty()
}