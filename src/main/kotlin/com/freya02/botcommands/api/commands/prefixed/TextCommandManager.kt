package com.freya02.botcommands.api.commands.prefixed

import com.freya02.botcommands.api.commands.prefixed.builder.TextCommandBuilder
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.commands.prefixed.TextCommandInfo

class TextCommandManager internal constructor(private val context: BContextImpl) {
    @get:JvmSynthetic
    internal val textCommands: MutableList<TextCommandInfo> = arrayListOf()

    fun textCommand(name: String, builder: TextCommandBuilder.() -> Unit) {
        textCommands += TextCommandBuilder(context, name)
            .apply(builder)
            .build(null)
    }
}