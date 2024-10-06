package io.github.freya022.botcommands.api.commands.options

import io.github.freya022.botcommands.api.core.Executable
import io.github.freya022.botcommands.api.core.options.Option

/**
 * Represents a Discord input of a command.
 */
interface CommandOption : Option {
    /**
     * The executable command this parameter is from.
     */
    @Deprecated("Renamed to 'executable'", ReplaceWith("executable"))
    val command: Executable
        get() = executable
}