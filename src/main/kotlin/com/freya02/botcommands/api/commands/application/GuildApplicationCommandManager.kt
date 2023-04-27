package com.freya02.botcommands.api.commands.application

import com.freya02.botcommands.api.commands.application.context.builder.MessageCommandBuilder
import com.freya02.botcommands.api.commands.application.context.builder.UserCommandBuilder
import com.freya02.botcommands.api.commands.application.slash.builder.TopLevelSlashCommandBuilder
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.throwUser
import net.dv8tion.jda.api.entities.Guild
import kotlin.reflect.KFunction

class GuildApplicationCommandManager internal constructor(val context: BContextImpl, val guild: Guild): IApplicationCommandManager() {
    override fun isValidScope(scope: CommandScope): Boolean = !scope.isGlobal

    //TODO move common part to IApplicationCommandManager, make checkScope function to throw with the custom message
    override fun slashCommand0(name: String, scope: CommandScope, function: KFunction<Any>?, builder: TopLevelSlashCommandBuilder.() -> Unit) {
        if (!isValidScope(scope)) throwUser("You can only use non-global scopes in a GuildApplicationCommandManager")

        TopLevelSlashCommandBuilder(context, name, function, scope)
            .apply(builder)
            .build()
            .also(::putNewCommand)
    }

    override fun userCommand0(name: String, scope: CommandScope, function: KFunction<Any>, builder: UserCommandBuilder.() -> Unit) {
        if (!isValidScope(scope)) throwUser("You can only use non-global scopes in a GuildApplicationCommandManager")

        UserCommandBuilder(context, name, function, scope)
            .apply(builder)
            .build()
            .also(::putNewCommand)
    }

    override fun messageCommand0(name: String, scope: CommandScope, function: KFunction<Any>, builder: MessageCommandBuilder.() -> Unit) {
        if (!isValidScope(scope)) throwUser("You can only use non-global scopes in a GuildApplicationCommandManager")

        MessageCommandBuilder(context, name, function, scope)
            .apply(builder)
            .build()
            .also(::putNewCommand)
    }
}