package io.github.freya022.botcommands.api.commands.application

import io.github.freya022.botcommands.api.commands.CommandPath

/**
 * Map of command paths to their application command
 */
interface CommandMap<T : ApplicationCommandInfo> : Map<CommandPath, T>