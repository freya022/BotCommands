package io.github.freya022.botcommands.api.commands.application.options

import io.github.freya022.botcommands.api.commands.application.ApplicationCommandInfo
import io.github.freya022.botcommands.api.commands.options.CommandOption

/**
 * Represents a Discord input option of an application command.
 */
interface ApplicationCommandOption : CommandOption {

    @Deprecated("Renamed to 'executable'", replaceWith = ReplaceWith("executable"))
    override val command get() = executable
    override val executable: ApplicationCommandInfo
}