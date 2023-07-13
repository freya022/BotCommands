package com.freya02.botcommands.api.commands.application

import com.freya02.botcommands.api.commands.application.context.annotations.JDAMessageCommand
import com.freya02.botcommands.api.commands.application.context.annotations.JDAUserCommand
import com.freya02.botcommands.api.commands.application.context.builder.MessageCommandBuilder
import com.freya02.botcommands.api.commands.application.context.builder.UserCommandBuilder
import com.freya02.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import com.freya02.botcommands.api.commands.application.slash.builder.TopLevelSlashCommandBuilder
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.commands.application.ApplicationCommandInfo
import com.freya02.botcommands.internal.commands.application.SimpleCommandMap
import kotlin.reflect.KFunction

sealed class AbstractApplicationCommandManager(private val context: BContextImpl) {
    private val commandMap: SimpleCommandMap<ApplicationCommandInfo> = SimpleCommandMap.ofInfos()
    internal val applicationCommands: Map<String, ApplicationCommandInfo>
        @JvmSynthetic get() = commandMap.map

    @JvmSynthetic
    internal abstract fun isValidScope(scope: CommandScope): Boolean

    protected abstract fun checkScope(scope: CommandScope)

    /**
     * **Annotation equivalent:** [JDASlashCommand]
     *
     * @see JDASlashCommand
     */
    @JvmOverloads
    fun slashCommand(name: String, scope: CommandScope = CommandScope.GLOBAL_NO_DM, function: KFunction<Any>?, builder: TopLevelSlashCommandBuilder.() -> Unit) {
        checkScope(scope)

        TopLevelSlashCommandBuilder(context, name, function, scope)
            .apply(builder)
            .build()
            .also(::putNewCommand)
    }

    /**
     * **Annotation equivalent:** [JDAUserCommand]
     *
     * @see JDAUserCommand
     */
    @JvmOverloads
    fun userCommand(name: String, scope: CommandScope = CommandScope.GLOBAL_NO_DM, function: KFunction<Any>, builder: UserCommandBuilder.() -> Unit) {
        checkScope(scope)

        UserCommandBuilder(context, name, function, scope)
            .apply(builder)
            .build()
            .also(::putNewCommand)
    }

    /**
     * **Annotation equivalent:** [JDAMessageCommand]
     *
     * @see JDAMessageCommand
     */
    @JvmOverloads
    fun messageCommand(name: String, scope: CommandScope = CommandScope.GLOBAL_NO_DM, function: KFunction<Any>, builder: MessageCommandBuilder.() -> Unit) {
        checkScope(scope)

        MessageCommandBuilder(context, name, function, scope)
            .apply(builder)
            .build()
            .also(::putNewCommand)
    }

    private fun putNewCommand(newInfo: ApplicationCommandInfo) {
        commandMap.putNewCommand(newInfo)
    }
}