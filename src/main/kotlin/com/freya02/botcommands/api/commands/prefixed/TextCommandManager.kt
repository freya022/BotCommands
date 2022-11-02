package com.freya02.botcommands.api.commands.prefixed

import com.freya02.botcommands.api.commands.prefixed.builder.TopLevelTextCommandBuilder
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.commands.prefixed.TopLevelTextCommandInfo

class TextCommandManager internal constructor(private val context: BContextImpl) {
    @get:JvmSynthetic
    internal val textCommands: MutableList<TopLevelTextCommandInfo> = arrayListOf()

    fun textCommand(name: String, builder: TopLevelTextCommandBuilder.() -> Unit) {
        textCommands += TopLevelTextCommandBuilder(context, name)
            .apply(builder)
            .build()
    }
}