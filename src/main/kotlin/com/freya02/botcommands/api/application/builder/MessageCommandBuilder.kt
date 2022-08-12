package com.freya02.botcommands.api.application.builder

import com.freya02.botcommands.api.application.CommandPath
import com.freya02.botcommands.api.application.CommandScope
import com.freya02.botcommands.api.application.slash.GeneratedValueSupplier
import com.freya02.botcommands.api.builder.GeneratedOptionBuilder
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.application.context.message.MessageCommandInfo

class MessageCommandBuilder internal constructor(private val context: BContextImpl, path: CommandPath, scope: CommandScope) :
    ApplicationCommandBuilder(path, scope) {

    /**
     * @param name Name of the declared parameter in the [function]
     */
    fun option(name: String) {
        optionBuilders[name] = MessageCommandOptionBuilder(name)
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

    internal fun build() = MessageCommandInfo(context, this)
}
