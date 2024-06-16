package io.github.freya022.botcommands.api.commands.application.context.user

import io.github.freya022.botcommands.api.commands.application.ApplicationCommandInfo
import io.github.freya022.botcommands.api.commands.application.TopLevelApplicationCommandInfo

/**
 * Represents a context command that acts on a user.
 */
interface UserCommandInfo : TopLevelApplicationCommandInfo, ApplicationCommandInfo {
    override val topLevelInstance: UserCommandInfo

    override val parameters: List<UserContextCommandParameter>

    override val discordOptions: List<UserContextCommandOption>
        get() = parameters.flatMap { it.allOptions }.filterIsInstance<UserContextCommandOption>()
}