package io.github.freya022.botcommands.api.commands.application

import io.github.freya022.botcommands.api.commands.*
import io.github.freya022.botcommands.api.commands.application.slash.SlashSubcommandGroupInfo
import io.github.freya022.botcommands.api.commands.application.slash.SlashSubcommandInfo
import io.github.freya022.botcommands.api.commands.application.slash.TopLevelSlashCommandInfo
import io.github.freya022.botcommands.api.core.Executable
import io.github.freya022.botcommands.api.core.entities.InputUser
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel

/**
 * Represents an application command of any kind.
 */
interface ApplicationCommandInfo : CommandInfo, Executable,
                                   ICommandParameterContainer, ICommandOptionContainer,
                                   IFilterContainer {
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

    /**
     * Returns a [Usability] instance, representing whether this application command can be used.
     */
    fun getUsability(inputUser: InputUser, channel: MessageChannel): Usability
}