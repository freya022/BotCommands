package com.freya02.botcommands.api.application.builder

import com.freya02.botcommands.api.application.CommandPath
import com.freya02.botcommands.api.application.CommandScope
import com.freya02.botcommands.api.application.slash.GeneratedValueSupplier
import com.freya02.botcommands.api.builder.GeneratedOptionBuilder
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.application.slash.SlashCommandInfo
import com.freya02.botcommands.internal.asDiscordString
import com.freya02.botcommands.internal.throwUser

class SlashCommandBuilder internal constructor(
    private val context: BContextImpl,
    path: CommandPath,
    scope: CommandScope
) : ApplicationCommandBuilder(path, scope) {
    var description: String = "No description"

    /**
     * @param declaredName Name of the declared parameter in the [function]
     */
    @JvmOverloads
    fun option(declaredName: String, optionName: String = declaredName.asDiscordString(), block: SlashCommandOptionBuilder.() -> Unit = {}) {
        optionBuilders[declaredName] = SlashCommandOptionBuilder(declaredName, optionName).apply(block)
    }

    /**
     * @param declaredName Name of the declared parameter in the [function]
     */
    override fun customOption(declaredName: String) {
        optionBuilders[declaredName] = CustomOptionBuilder(declaredName)
    }

    /**
     * @param declaredName Name of the declared parameter in the [function]
     */
    override fun generatedOption(declaredName: String, generatedValueSupplier: GeneratedValueSupplier) {
        optionBuilders[declaredName] = GeneratedOptionBuilder(declaredName, generatedValueSupplier)
    }

    internal fun build(): SlashCommandInfo {
        if (!isFunctionInitialized()) {
            throwUser("A command must have its function set")
        }

        return SlashCommandInfo(context, this)
    }
}
