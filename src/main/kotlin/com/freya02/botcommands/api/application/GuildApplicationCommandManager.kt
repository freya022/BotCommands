package com.freya02.botcommands.api.application

import com.freya02.botcommands.api.application.builder.SlashCommandBuilder
import com.freya02.botcommands.internal.BContextImpl
import net.dv8tion.jda.api.entities.Guild

class GuildApplicationCommandManager internal constructor(private val context: BContextImpl, val guild: Guild): IApplicationCommandManager {
    override fun slashCommand(path: CommandPath, builder: SlashCommandBuilder.() -> Unit) {
        context.addSlashCommand(
            SlashCommandBuilder(context, path)
                .apply(builder)
                .build()
        )
    }
}