package com.freya02.botcommands.api.commands.application

import com.freya02.botcommands.api.commands.application.context.builder.MessageCommandBuilder
import com.freya02.botcommands.api.commands.application.context.builder.UserCommandBuilder
import com.freya02.botcommands.api.commands.application.slash.builder.TopLevelSlashCommandBuilder
import com.freya02.botcommands.internal.commands.application.ApplicationCommandInfo
import com.freya02.botcommands.internal.commands.application.SimpleCommandMap
import kotlin.reflect.KFunction

//TODO Rename to AbstractApplicationCommandManager
sealed class IApplicationCommandManager {
    private val commandMap: SimpleCommandMap<ApplicationCommandInfo> = SimpleCommandMap.ofInfos()
    internal val applicationCommands: Map<String, ApplicationCommandInfo>
        @JvmSynthetic get() = commandMap.map

    @JvmSynthetic
    internal abstract fun isValidScope(scope: CommandScope): Boolean

    protected abstract fun slashCommand0(name: String, scope: CommandScope, function: KFunction<Any>, builder: TopLevelSlashCommandBuilder.() -> Unit)
    protected abstract fun userCommand0(name: String, scope: CommandScope, function: KFunction<Any>, builder: UserCommandBuilder.() -> Unit)
    protected abstract fun messageCommand0(name: String, scope: CommandScope, function: KFunction<Any>, builder: MessageCommandBuilder.() -> Unit)

    @JvmOverloads
    fun slashCommand(name: String, scope: CommandScope = CommandScope.GLOBAL_NO_DM, function: KFunction<Any>, builder: TopLevelSlashCommandBuilder.() -> Unit) {
        slashCommand0(name, scope, function, builder)
    }

    @JvmOverloads
    fun userCommand(name: String, scope: CommandScope = CommandScope.GLOBAL_NO_DM, function: KFunction<Any>, builder: UserCommandBuilder.() -> Unit) {
        userCommand0(name, scope, function, builder)
    }

    @JvmOverloads
    fun messageCommand(name: String, scope: CommandScope = CommandScope.GLOBAL_NO_DM, function: KFunction<Any>, builder: MessageCommandBuilder.() -> Unit) {
        messageCommand0(name, scope, function, builder)
    }

    protected fun putNewCommand(newInfo: ApplicationCommandInfo) {
        commandMap.putNewCommand(newInfo)
    }
}