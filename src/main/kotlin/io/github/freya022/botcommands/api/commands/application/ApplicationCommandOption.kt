package io.github.freya022.botcommands.api.commands.application

import io.github.freya022.botcommands.api.commands.CommandOption

/**
 * Represents a Discord input option of an application command.
 */
interface ApplicationCommandOption : CommandOption {
    override val command: ApplicationCommandInfo
}