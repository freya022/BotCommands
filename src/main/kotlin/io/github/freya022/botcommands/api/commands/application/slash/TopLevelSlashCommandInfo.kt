package io.github.freya022.botcommands.api.commands.application.slash

import io.github.freya022.botcommands.api.commands.application.TopLevelApplicationCommandInfo

interface TopLevelSlashCommandInfo : TopLevelApplicationCommandInfo, SlashCommandInfo {
    override val topLevelInstance: TopLevelSlashCommandInfo

    val subcommands: Map<String, SlashSubcommandInfo>
    val subcommandGroups: Map<String, SlashSubcommandGroupInfo>

    val isTopLevelCommandOnly: Boolean
        get() = subcommands.isEmpty() && subcommandGroups.isEmpty()
}