package com.freya02.botcommands.api.commands.application

import com.freya02.botcommands.api.application.CommandPath
import com.freya02.botcommands.api.application.CommandScope
import com.freya02.botcommands.api.commands.application.context.builder.MessageCommandBuilder
import com.freya02.botcommands.api.commands.application.context.builder.UserCommandBuilder
import com.freya02.botcommands.api.commands.application.slash.builder.SlashCommandBuilder
import com.freya02.botcommands.internal.commands.application.ApplicationCommandInfo

sealed class IApplicationCommandManager {
    internal abstract val applicationCommands: List<ApplicationCommandInfo>

    protected abstract fun slashCommand0(path: CommandPath, scope: CommandScope, builder: SlashCommandBuilder.() -> Unit)
    protected abstract fun userCommand0(name: String, scope: CommandScope, builder: UserCommandBuilder.() -> Unit)
    protected abstract fun messageCommand0(name: String, scope: CommandScope, builder: MessageCommandBuilder.() -> Unit)

    @JvmOverloads
    fun slashCommand(path: CommandPath, scope: CommandScope = CommandScope.GLOBAL_NO_DM, builder: SlashCommandBuilder.() -> Unit) {
        slashCommand0(path, scope, builder)
    }

    @JvmOverloads
    fun slashCommand(name: String, group: String? = null, subcommand: String? = null, scope: CommandScope = CommandScope.GLOBAL_NO_DM, builder: SlashCommandBuilder.() -> Unit) {
        slashCommand0(CommandPath.of(name, group, subcommand), scope, builder)
    }

    @JvmOverloads
    fun userCommand(name: String, scope: CommandScope = CommandScope.GLOBAL_NO_DM, builder: UserCommandBuilder.() -> Unit) {
        userCommand0(name, scope, builder)
    }

    @JvmOverloads
    fun messageCommand(name: String, scope: CommandScope = CommandScope.GLOBAL_NO_DM, builder: MessageCommandBuilder.() -> Unit) {
        messageCommand0(name, scope, builder)
    }
}