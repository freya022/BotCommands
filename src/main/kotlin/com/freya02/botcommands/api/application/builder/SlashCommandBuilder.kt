package com.freya02.botcommands.api.application.builder

import com.freya02.botcommands.api.application.CommandPath
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.application.slash.SlashCommandInfo
import com.freya02.botcommands.internal.throwUser

class SlashCommandBuilder internal constructor(
    private val context: BContextImpl,
    instance: Any,
    path: CommandPath
) : ApplicationCommandBuilder(instance, path) {
    var description: String = "No description"
    internal val optionBuilders: MutableList<SlashCommandOptionBuilder> = mutableListOf()

    fun option(name: String, block: SlashCommandOptionBuilder.() -> Unit = {}) {
        optionBuilders.add(SlashCommandOptionBuilder(name).apply(block))
    }

    internal fun build(): SlashCommandInfo {
        if (!isFunctionInitialized()) {
            throwUser("A command must have its function set")
        }

        return SlashCommandInfo(context, this)
    }
}
