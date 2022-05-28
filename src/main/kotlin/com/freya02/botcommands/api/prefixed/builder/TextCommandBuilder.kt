package com.freya02.botcommands.api.prefixed.builder

import com.freya02.botcommands.api.application.CommandPath
import com.freya02.botcommands.api.application.builder.OptionBuilder
import com.freya02.botcommands.api.builder.CommandBuilder
import com.freya02.botcommands.internal.prefixed.TextCommandInfo

class TextCommandBuilder internal constructor(instance: Any, path: CommandPath) : CommandBuilder(instance, path) {
    var ownerRequired: Boolean = false
    var hidden: Boolean = false
    var aliases: List<CommandPath> = listOf()
    var description = "No description"
    var order = -1

    override val optionBuilders: MutableMap<String, OptionBuilder> = mutableMapOf() //TODO text option builder

    internal fun build(): TextCommandInfo {
        TODO()
    }
}
