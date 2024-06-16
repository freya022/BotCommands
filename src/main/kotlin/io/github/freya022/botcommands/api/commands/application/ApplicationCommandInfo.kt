package io.github.freya022.botcommands.api.commands.application

import io.github.freya022.botcommands.api.commands.CommandInfo
import io.github.freya022.botcommands.api.commands.ICommandOptionContainer
import io.github.freya022.botcommands.api.commands.IFilterContainer
import io.github.freya022.botcommands.api.commands.application.slash.SlashSubcommandGroupInfo
import io.github.freya022.botcommands.api.commands.application.slash.SlashSubcommandInfo
import io.github.freya022.botcommands.api.commands.application.slash.TopLevelSlashCommandInfo
import io.github.freya022.botcommands.api.core.Executable

/**
 * Represents an application command of any kind.
 */
interface ApplicationCommandInfo : CommandInfo, Executable, ICommandOptionContainer, IFilterContainer {
    /**
     * Retrieves the top-level command owning this application command.
     */
    val topLevelInstance: TopLevelApplicationCommandInfo

    /**
     * Returns the full command name of this application command, separate with spaces.
     *
     * Examples for `/docs search jda`:
     * - [TopLevelSlashCommandInfo] == `docs`
     * - [SlashSubcommandInfo] == `docs search jda`
     * - [SlashSubcommandGroupInfo] == `docs search`
     */
    val fullCommandName: String get() = path.getFullPath(' ')
}