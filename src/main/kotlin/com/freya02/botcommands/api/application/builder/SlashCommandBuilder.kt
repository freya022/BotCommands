package com.freya02.botcommands.api.application.builder

import com.freya02.botcommands.api.application.CommandPath
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.application.slash.SlashCommandInfo

class SlashCommandBuilder internal constructor(private val context: BContextImpl, instance: Any, path: CommandPath) :
    ApplicationCommandBuilder(instance, path) {
    var description: String = "No description"

    internal fun build(): SlashCommandInfo {
        return SlashCommandInfo(context, this)
    }
}
