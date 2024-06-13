package io.github.freya022.botcommands.api.commands.application.context.user

import io.github.freya022.botcommands.api.commands.application.ApplicationCommandInfo
import io.github.freya022.botcommands.api.commands.application.TopLevelApplicationCommandInfo

interface UserCommandInfo : TopLevelApplicationCommandInfo, ApplicationCommandInfo {
    override val parameters: List<UserContextCommandParameter>

    val allDiscordOptions: List<UserContextCommandOption>
        get() = parameters.flatMap { it.allOptions }.filterIsInstance<UserContextCommandOption>()
}