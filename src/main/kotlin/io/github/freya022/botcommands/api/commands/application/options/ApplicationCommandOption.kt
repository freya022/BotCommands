package io.github.freya022.botcommands.api.commands.application.options

import io.github.freya022.botcommands.api.commands.options.CommandOption

/**
 * Represents a Discord input option of an application command.
 */
interface ApplicationCommandOption : CommandOption {

    override val executable get() = parent.executable
    override val parent: ApplicationCommandParameter
}