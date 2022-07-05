package com.freya02.botcommands.api.application

import com.freya02.botcommands.api.application.builder.SlashCommandBuilder
import com.freya02.botcommands.core.api.annotations.BService
import com.freya02.botcommands.internal.BContextImpl

@BService
@Deprecated("To be removed") //TODO remove
class ApplicationCommandManager internal constructor(val context: BContextImpl) {
    fun slashCommand(path: CommandPath, builder: SlashCommandBuilder.() -> Unit) {
        context.addSlashCommand(
            SlashCommandBuilder(context, path, TODO())
                .apply(builder)
                .build()
        )
    }
}