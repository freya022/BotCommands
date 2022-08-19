package com.freya02.botcommands.api.commands.prefixed

import com.freya02.botcommands.api.commands.application.CommandPath
import com.freya02.botcommands.api.commands.prefixed.builder.TextCommandBuilder
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.commands.prefixed.TextCommandInfo

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