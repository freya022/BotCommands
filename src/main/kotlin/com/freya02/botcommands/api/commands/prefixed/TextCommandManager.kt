package com.freya02.botcommands.api.commands.prefixed

import com.freya02.botcommands.api.commands.prefixed.builder.TopLevelTextCommandBuilder
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.commands.application.SimpleCommandMap
import com.freya02.botcommands.internal.commands.prefixed.TopLevelTextCommandInfo

class TextCommandManager internal constructor(private val context: BContextImpl) {
    @get:JvmSynthetic
    internal val textCommands: SimpleCommandMap<TopLevelTextCommandInfo> = SimpleCommandMap(null)

    fun textCommand(name: String, builder: TopLevelTextCommandBuilder.() -> Unit) {
        TopLevelTextCommandBuilder(context, name)
            .apply(builder)
            .build()
            .also(textCommands::putNewCommand)
    }
}