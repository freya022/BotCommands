package com.freya02.botcommands.api.application.builder

import com.freya02.botcommands.api.application.CommandPath
import com.freya02.botcommands.api.application.CommandScope
import com.freya02.botcommands.api.application.slash.GeneratedValueSupplier
import com.freya02.botcommands.api.builder.GeneratedOptionBuilder
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.application.slash.SlashCommandInfo
import com.freya02.botcommands.internal.throwUser

class SlashCommandBuilder internal constructor(
    private val context: BContextImpl,
    path: CommandPath,
    scope: CommandScope
) : ApplicationCommandBuilder(path, scope) {
    var description: String = "No description"

    /**
     * @param name Name of the declared parameter in the [function]
     */
    fun option(name: String, block: SlashCommandOptionBuilder.() -> Unit = {}) {
        optionBuilders[name] = SlashCommandOptionBuilder(name).apply(block)
    }

    /**
     * @param name Name of the declared parameter in the [function]
     */
    override fun customOption(name: String) {
        optionBuilders[name] = CustomOptionBuilder(name)
    }

    /**
     * @param name Name of the declared parameter in the [function]
     */
    override fun generatedOption(name: String, generatedValueSupplier: GeneratedValueSupplier) {
        optionBuilders[name] = GeneratedOptionBuilder(name, generatedValueSupplier)
    }

    internal fun build(): SlashCommandInfo {
        if (!isFunctionInitialized()) {
            throwUser("A command must have its function set")
        }

        return SlashCommandInfo(context, this)
    }
}
