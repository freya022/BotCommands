package com.freya02.botcommands.api.prefixed.builder

import com.freya02.botcommands.api.application.CommandPath
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.prefixed.TextCommandInfo

class TextCommandManager internal constructor(private val context: BContextImpl) {
    @get:JvmSynthetic
    internal val textCommands: MutableList<TextCommandInfo> = arrayListOf()

    fun textCommand(path: CommandPath, builder: TextCommandBuilder.() -> Unit) {
        textCommands += TextCommandBuilder(context, path)
            .apply(builder)
            .build()
    }

    @JvmOverloads
    fun textCommand(name: String, group: String? = null, subcommand: String? = null, builder: TextCommandBuilder.() -> Unit) {
        textCommand(CommandPath.of(name, group, subcommand), builder)
    }
}