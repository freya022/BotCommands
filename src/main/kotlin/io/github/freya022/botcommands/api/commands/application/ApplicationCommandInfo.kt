package io.github.freya022.botcommands.api.commands.application

import io.github.freya022.botcommands.api.commands.CommandInfo
import io.github.freya022.botcommands.api.commands.ICommandOptionContainer
import io.github.freya022.botcommands.api.commands.IFilterContainer
import io.github.freya022.botcommands.api.core.Executable

interface ApplicationCommandInfo : CommandInfo, Executable, ICommandOptionContainer, IFilterContainer {
    val topLevelInstance: TopLevelApplicationCommandInfo

    val fullCommandName: String get() = path.getFullPath(' ')
}