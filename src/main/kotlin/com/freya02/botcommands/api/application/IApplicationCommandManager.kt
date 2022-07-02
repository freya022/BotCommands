package com.freya02.botcommands.api.application

import com.freya02.botcommands.api.application.builder.SlashCommandBuilder
import com.freya02.botcommands.internal.application.ApplicationCommandInfo

sealed interface IApplicationCommandManager {
    val guildApplicationCommands: List<ApplicationCommandInfo>

    fun slashCommand(path: CommandPath, builder: SlashCommandBuilder.() -> Unit)
}