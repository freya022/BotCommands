package com.freya02.botcommands.api.application

import com.freya02.botcommands.api.application.builder.SlashCommandBuilder

sealed interface IApplicationCommandManager {
    fun slashCommand(path: CommandPath, builder: SlashCommandBuilder.() -> Unit)
}