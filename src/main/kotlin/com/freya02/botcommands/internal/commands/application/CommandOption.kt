package com.freya02.botcommands.internal.commands.application

import com.freya02.botcommands.api.commands.CommandOptionBuilder
import com.freya02.botcommands.internal.core.options.OptionImpl

abstract class CommandOption internal constructor(
    commandOptionBuilder: CommandOptionBuilder
) : OptionImpl(commandOptionBuilder) {
    abstract val resolver: Any
}