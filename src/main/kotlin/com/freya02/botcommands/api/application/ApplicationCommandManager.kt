package com.freya02.botcommands.api.application

import com.freya02.botcommands.api.application.builder.SlashCommandBuilder
import com.freya02.botcommands.internal.BContextImpl

class ApplicationCommandManager internal constructor(val context: BContextImpl) {
    fun slashCommand(path: CommandPath, builder: SlashCommandBuilder.() -> Unit) {
        context.addSlashCommand(
            SlashCommandBuilder(context, path)
                .apply(builder)
                .build()
        )
    }
}