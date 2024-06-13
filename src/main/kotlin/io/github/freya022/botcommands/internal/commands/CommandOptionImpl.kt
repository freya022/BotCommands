package io.github.freya022.botcommands.internal.commands

import io.github.freya022.botcommands.api.commands.CommandOption
import io.github.freya022.botcommands.api.commands.CommandOptionBuilder
import io.github.freya022.botcommands.internal.core.options.OptionImpl

internal abstract class CommandOptionImpl internal constructor(
    commandOptionBuilder: CommandOptionBuilder
) : OptionImpl(commandOptionBuilder),
    CommandOption