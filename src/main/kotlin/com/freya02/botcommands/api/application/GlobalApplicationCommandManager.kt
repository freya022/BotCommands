package com.freya02.botcommands.api.application

import com.freya02.botcommands.api.application.builder.SlashCommandBuilder
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.application.ApplicationCommandInfo

class GlobalApplicationCommandManager internal constructor(val context: BContextImpl): IApplicationCommandManager {
    override val guildApplicationCommands: ArrayList<ApplicationCommandInfo> = arrayListOf()

    override fun slashCommand(path: CommandPath, builder: SlashCommandBuilder.() -> Unit) {
        guildApplicationCommands += SlashCommandBuilder(context, path)
            .apply(builder)
            .build()
    }
}