package io.github.freya022.botcommands.internal.commands.application

import io.github.freya022.botcommands.api.commands.CommandOptionBuilder
import io.github.freya022.botcommands.internal.core.options.OptionImpl

abstract class CommandOption internal constructor(
    commandOptionBuilder: CommandOptionBuilder
) : OptionImpl(commandOptionBuilder) {
    abstract val resolver: Any
}