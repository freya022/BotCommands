package com.freya02.botcommands.api.prefixed.builder

import com.freya02.botcommands.api.application.CommandPath
import com.freya02.botcommands.api.builder.CommandBuilder
import com.freya02.botcommands.internal.prefixed.TextCommandInfo

class TextCommandBuilder internal constructor(path: CommandPath) : CommandBuilder(path) {
    var ownerRequired: Boolean = false
    var aliases: List<CommandPath> = listOf()
    var description = "No description"
    var order = -1

    internal fun build(): TextCommandInfo {
        TODO()
    }
}
