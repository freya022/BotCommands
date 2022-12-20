package com.freya02.botcommands.api.commands.application

import com.freya02.botcommands.api.commands.application.context.builder.MessageCommandBuilder
import com.freya02.botcommands.api.commands.application.context.builder.UserCommandBuilder
import com.freya02.botcommands.api.commands.application.slash.builder.TopLevelSlashCommandBuilder
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.throwUser

class GlobalApplicationCommandManager internal constructor(val context: BContextImpl): IApplicationCommandManager() {
    override fun isValidScope(scope: CommandScope) = scope.isGlobal

    override fun slashCommand0(name: String, scope: CommandScope, builder: TopLevelSlashCommandBuilder.() -> Unit) {
        if (!isValidScope(scope)) throwUser("You can only use global scopes in a GlobalApplicationCommandManager")

        TopLevelSlashCommandBuilder(context, name, scope)
            .apply(builder)
            .build()
            .also(::putNewCommand)
    }

    override fun userCommand0(name: String, scope: CommandScope, builder: UserCommandBuilder.() -> Unit) {
        if (!isValidScope(scope)) throwUser("You can only use global scopes in a GlobalApplicationCommandManager")

        UserCommandBuilder(context, name, scope)
            .apply(builder)
            .build()
            .also(::putNewCommand)
    }

    override fun messageCommand0(name: String, scope: CommandScope, builder: MessageCommandBuilder.() -> Unit) {
        if (!isValidScope(scope)) throwUser("You can only use global scopes in a GlobalApplicationCommandManager")

        MessageCommandBuilder(context, name, scope)
            .apply(builder)
            .build()
            .also(::putNewCommand)
    }
}