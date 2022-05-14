package com.freya02.botcommands.api.application

import com.freya02.botcommands.api.application.builder.SlashCommandBuilder
import com.freya02.botcommands.internal.BContextImpl

class ApplicationCommandManager internal constructor(val context: BContextImpl) {
    fun slashCommand(path: CommandPath, instance: Any, builder: SlashCommandBuilder.() -> Unit) {
        context.addSlashCommand(
            SlashCommandBuilder(context, instance, path)
                .apply(builder)
                .build()
        )
    }
}