package io.github.freya022.botcommands.api.commands.options

import io.github.freya022.botcommands.api.core.options.Option

/**
 * Represents a Discord input of a command.
 */
interface CommandOption : Option {

    override val parent: CommandParameter
}