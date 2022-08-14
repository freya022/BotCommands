package com.freya02.botcommands.api.prefixed.builder

import com.freya02.botcommands.api.application.CommandPath
import com.freya02.botcommands.api.builder.CommandBuilder
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.prefixed.TextCommandInfo
import com.freya02.botcommands.internal.throwUser

class TextCommandBuilder internal constructor(private val context: BContextImpl, path: CommandPath) : CommandBuilder(path) {
    var ownerRequired: Boolean = false
    var hidden: Boolean = false
    var aliases: List<CommandPath> = listOf()
    var description = "No description"
    var order = -1

    internal fun build(): TextCommandInfo {
        if (!isFunctionInitialized()) {
            throwUser("A command must have its function set")
        }

        return TextCommandInfo(context, this)
    }
}
