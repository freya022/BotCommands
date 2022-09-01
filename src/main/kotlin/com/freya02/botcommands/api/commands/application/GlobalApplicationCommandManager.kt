package com.freya02.botcommands.api.commands.application

import com.freya02.botcommands.api.commands.application.context.builder.MessageCommandBuilder
import com.freya02.botcommands.api.commands.application.context.builder.UserCommandBuilder
import com.freya02.botcommands.api.commands.application.slash.builder.TopLevelSlashCommandBuilder
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.commands.application.ApplicationCommandInfo
import com.freya02.botcommands.internal.throwUser

class GlobalApplicationCommandManager internal constructor(val context: BContextImpl): IApplicationCommandManager() {
    override val applicationCommands: MutableList<ApplicationCommandInfo> = arrayListOf()

    override fun slashCommand0(name: String, scope: CommandScope, builder: TopLevelSlashCommandBuilder.() -> Unit) {
        if (!scope.isGlobal) throwUser("You can only use global scopes in a GlobalApplicationCommandManager")

        applicationCommands += TopLevelSlashCommandBuilder(context, name, scope)
            .apply(builder)
            .build()
    }

    override fun userCommand0(name: String, scope: CommandScope, builder: UserCommandBuilder.() -> Unit) {
        if (!scope.isGlobal) throwUser("You can only use global scopes in a GlobalApplicationCommandManager")

        applicationCommands += UserCommandBuilder(context, name, scope)
            .apply(builder)
            .build()
    }

    override fun messageCommand0(name: String, scope: CommandScope, builder: MessageCommandBuilder.() -> Unit) {
        if (!scope.isGlobal) throwUser("You can only use global scopes in a GlobalApplicationCommandManager")

        applicationCommands += MessageCommandBuilder(context, name, scope)
            .apply(builder)
            .build()
    }
}