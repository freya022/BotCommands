package com.freya02.botcommands.api.commands.application

import com.freya02.botcommands.api.commands.application.context.builder.MessageCommandBuilder
import com.freya02.botcommands.api.commands.application.context.builder.UserCommandBuilder
import com.freya02.botcommands.api.commands.application.slash.builder.TopLevelSlashCommandBuilder
import com.freya02.botcommands.internal.commands.application.ApplicationCommandInfo
import com.freya02.botcommands.internal.throwUser
import com.freya02.botcommands.internal.utils.ReflectionUtilsKt.shortSignature

sealed class IApplicationCommandManager {
    private val mutableApplicationCommands: MutableMap<String, ApplicationCommandInfo> = hashMapOf()
    internal val applicationCommands: Map<String, ApplicationCommandInfo>
        @JvmSynthetic get() = mutableApplicationCommands

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

    protected fun putNewCommand(newInfo: ApplicationCommandInfo) {
        mutableApplicationCommands.putIfAbsent(newInfo.name, newInfo)?.let { oldInfo ->
            throwUser(
                """
                Top level command '${newInfo.name}' is already defined
                Existing command: ${oldInfo.method.shortSignature}
                Current command: ${newInfo.method.shortSignature}
                """.trimIndent()
            )
        }
    }
}