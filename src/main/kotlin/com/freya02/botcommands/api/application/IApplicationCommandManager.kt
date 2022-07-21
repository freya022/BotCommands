package com.freya02.botcommands.api.application

import com.freya02.botcommands.api.application.builder.SlashCommandBuilder
import com.freya02.botcommands.internal.application.ApplicationCommandInfo

sealed class IApplicationCommandManager {
    abstract val guildApplicationCommands: List<ApplicationCommandInfo>

    internal abstract fun slashCommand0(path: CommandPath, scope: CommandScope, builder: SlashCommandBuilder.() -> Unit)

    @JvmOverloads
    fun slashCommand(path: CommandPath, scope: CommandScope = CommandScope.GLOBAL_NO_DM, builder: SlashCommandBuilder.() -> Unit) {
        slashCommand0(path, scope, builder)
    }

    @JvmOverloads
    fun slashCommand(name: String, group: String? = null, subcommand: String? = null, scope: CommandScope = CommandScope.GLOBAL_NO_DM, builder: SlashCommandBuilder.() -> Unit) {
        slashCommand0(CommandPath.of(name, group, subcommand), scope, builder)
    }
}