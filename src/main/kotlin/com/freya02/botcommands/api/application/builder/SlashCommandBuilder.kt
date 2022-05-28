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
    override val optionBuilders: MutableMap<String, OptionBuilder> = mutableMapOf()

    fun option(name: String, block: SlashCommandOptionBuilder.() -> Unit = {}) {
        optionBuilders[name] = SlashCommandOptionBuilder(name).apply(block)
    }

    fun customOption(name: String, block: CustomOptionBuilder.() -> Unit = {}) {
        optionBuilders[name] = CustomOptionBuilder(name).apply(block)
    }

    internal fun build(): SlashCommandInfo {
        if (!isFunctionInitialized()) {
            throwUser("A command must have its function set")
        }

        return SlashCommandInfo(context, this)
    }
}
