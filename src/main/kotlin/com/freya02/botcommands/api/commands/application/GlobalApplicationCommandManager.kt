package com.freya02.botcommands.api.commands.application

import com.freya02.botcommands.api.commands.application.context.builder.MessageCommandBuilder
import com.freya02.botcommands.api.commands.application.context.builder.UserCommandBuilder
import com.freya02.botcommands.api.commands.application.slash.builder.TopLevelSlashCommandBuilder
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.throwUser
import kotlin.reflect.KFunction

class GlobalApplicationCommandManager internal constructor(val context: BContextImpl): IApplicationCommandManager() {
    override fun isValidScope(scope: CommandScope) = scope.isGlobal

    override fun slashCommand0(name: String, scope: CommandScope, function: KFunction<Any>?, builder: TopLevelSlashCommandBuilder.() -> Unit) {
        if (!isValidScope(scope)) throwUser("You can only use global scopes in a GlobalApplicationCommandManager")

        TopLevelSlashCommandBuilder(context, name, function, scope)
            .apply(builder)
            .build()
            .also(::putNewCommand)
    }

    override fun userCommand0(name: String, scope: CommandScope, function: KFunction<Any>, builder: UserCommandBuilder.() -> Unit) {
        if (!isValidScope(scope)) throwUser("You can only use global scopes in a GlobalApplicationCommandManager")

        UserCommandBuilder(context, name, function, scope)
            .apply(builder)
            .build()
            .also(::putNewCommand)
    }

    override fun messageCommand0(name: String, scope: CommandScope, function: KFunction<Any>, builder: MessageCommandBuilder.() -> Unit) {
        if (!isValidScope(scope)) throwUser("You can only use global scopes in a GlobalApplicationCommandManager")

        MessageCommandBuilder(context, name, function, scope)
            .apply(builder)
            .build()
            .also(::putNewCommand)
    }
}