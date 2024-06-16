package io.github.freya022.botcommands.api.commands

import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.Executable
import io.github.freya022.botcommands.api.core.options.Option

/**
 * Represents a Discord input of a command.
 */
interface CommandOption : Option {
    /**
     * The main context.
     */
    val context: BContext

    /**
     * The executable command this parameter is from.
     */
    val command: Executable
}