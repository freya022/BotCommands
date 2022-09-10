package com.freya02.botcommands.api.commands.application

import com.freya02.botcommands.api.commands.application.context.builder.MessageCommandBuilder
import com.freya02.botcommands.api.commands.application.context.builder.UserCommandBuilder
import com.freya02.botcommands.api.commands.application.slash.builder.TopLevelSlashCommandBuilder
import com.freya02.botcommands.internal.commands.application.ApplicationCommandInfo

sealed class IApplicationCommandManager {
    internal abstract val applicationCommands: List<ApplicationCommandInfo>

    @JvmSynthetic
    internal abstract fun isValidScope(scope: CommandScope): Boolean

    protected abstract fun slashCommand0(name: String, scope: CommandScope, builder: TopLevelSlashCommandBuilder.() -> Unit)
    protected abstract fun userCommand0(name: String, scope: CommandScope, builder: UserCommandBuilder.() -> Unit)
    protected abstract fun messageCommand0(name: String, scope: CommandScope, builder: MessageCommandBuilder.() -> Unit)

    @JvmOverloads
    fun slashCommand(name: String, scope: CommandScope = CommandScope.GLOBAL_NO_DM, builder: TopLevelSlashCommandBuilder.() -> Unit) {
        slashCommand0(name, scope, builder)
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