package io.github.freya022.botcommands.api.commands

import io.github.freya022.botcommands.api.commands.options.CommandOption
import io.github.freya022.botcommands.api.core.Executable

/**
 * Holds Discord input options.
 */
interface ICommandOptionContainer : Executable {
    /**
     * All options representing a Discord input.
     */
    val discordOptions: List<CommandOption>
}