package io.github.freya022.botcommands.api.commands.application

import io.github.freya022.botcommands.api.commands.CommandInfo
import io.github.freya022.botcommands.api.core.Executable

interface ApplicationCommandInfo : CommandInfo, Executable {
    val topLevelInstance: TopLevelApplicationCommandInfo

    val fullCommandName: String get() = path.getFullPath(' ')
}