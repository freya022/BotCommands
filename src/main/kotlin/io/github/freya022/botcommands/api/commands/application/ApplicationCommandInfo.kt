package io.github.freya022.botcommands.api.commands.application

import io.github.freya022.botcommands.api.commands.CommandInfo
import io.github.freya022.botcommands.api.commands.Executable

interface ApplicationCommandInfo : CommandInfo, Executable {
    val topLevelInstance: TopLevelApplicationCommandInfo
}