package com.freya02.botcommands.api.prefixed.builder

import com.freya02.botcommands.api.application.CommandPath
import com.freya02.botcommands.api.application.builder.CustomOptionBuilder
import com.freya02.botcommands.api.builder.CommandBuilder
import com.freya02.botcommands.api.builder.TextGeneratedOptionBuilder
import com.freya02.botcommands.api.prefixed.TextGeneratedValueSupplier
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.asDiscordString
import com.freya02.botcommands.internal.prefixed.TextCommandInfo
import com.freya02.botcommands.internal.throwUser

class TextCommandBuilder internal constructor(private val context: BContextImpl, path: CommandPath) : CommandBuilder(path) {
    var ownerRequired: Boolean = false
    var hidden: Boolean = false
    var aliases: MutableList<CommandPath> = arrayListOf()
    var description = "No description"
    var order = -1

    /**
     * @param declaredName Name of the declared parameter in the [function]
     */
    @JvmOverloads
    fun option(declaredName: String, optionName: String = declaredName.asDiscordString(), block: TextOptionBuilder.() -> Unit = {}) {
        optionBuilders[declaredName] = TextOptionBuilder(declaredName, optionName).apply(block)
    }

    /**
     * @param declaredName Name of the declared parameter in the [function]
     */
    fun customOption(declaredName: String) {
        optionBuilders[declaredName] = CustomOptionBuilder(declaredName)
    }

    /**
     * @param declaredName Name of the declared parameter in the [function]
     */
    fun generatedOption(declaredName: String, generatedValueSupplier: TextGeneratedValueSupplier) {
        optionBuilders[declaredName] = TextGeneratedOptionBuilder(declaredName, generatedValueSupplier)
    }

    internal fun build(): TextCommandInfo {
        if (!isFunctionInitialized()) {
            throwUser("A command must have its function set")
        }

        return TextCommandInfo(context, this)
    }
}
