package io.github.freya022.botcommands.api.commands.application.context.message

import io.github.freya022.botcommands.api.commands.application.ApplicationCommandInfo
import io.github.freya022.botcommands.api.commands.application.TopLevelApplicationCommandInfo

/**
 * Represents a context command that acts on a message.
 */
interface MessageCommandInfo : TopLevelApplicationCommandInfo, ApplicationCommandInfo {
    override val topLevelInstance: MessageCommandInfo

    override val parameters: List<MessageContextCommandParameter>

    override val discordOptions: List<MessageContextCommandOption>
        get() = parameters.flatMap { it.allOptions }.filterIsInstance<MessageContextCommandOption>()
}