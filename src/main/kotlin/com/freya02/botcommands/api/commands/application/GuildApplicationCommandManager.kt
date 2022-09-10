package com.freya02.botcommands.api.commands.application

import com.freya02.botcommands.api.commands.application.context.builder.MessageCommandBuilder
import com.freya02.botcommands.api.commands.application.context.builder.UserCommandBuilder
import com.freya02.botcommands.api.commands.application.slash.builder.TopLevelSlashCommandBuilder
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.commands.application.ApplicationCommandInfo
import com.freya02.botcommands.internal.throwUser
import net.dv8tion.jda.api.entities.Guild

class GuildApplicationCommandManager internal constructor(val context: BContextImpl, val guild: Guild): IApplicationCommandManager() {
    override val applicationCommands: MutableList<ApplicationCommandInfo> = arrayListOf()

    override fun isValidScope(scope: CommandScope): Boolean = !scope.isGlobal

    override fun slashCommand0(name: String, scope: CommandScope, builder: TopLevelSlashCommandBuilder.() -> Unit) {
        if (!isValidScope(scope)) throwUser("You can only use non-global scopes in a GuildApplicationCommandManager")

        applicationCommands += TopLevelSlashCommandBuilder(context, name, scope)
            .apply(builder)
            .build()
    }

    override fun userCommand0(name: String, scope: CommandScope, builder: UserCommandBuilder.() -> Unit) {
        if (!isValidScope(scope)) throwUser("You can only use non-global scopes in a GuildApplicationCommandManager")

        applicationCommands += UserCommandBuilder(context, name, scope)
            .apply(builder)
            .build()
    }

    override fun messageCommand0(name: String, scope: CommandScope, builder: MessageCommandBuilder.() -> Unit) {
        if (!isValidScope(scope)) throwUser("You can only use non-global scopes in a GuildApplicationCommandManager")

        applicationCommands += MessageCommandBuilder(context, name, scope)
            .apply(builder)
            .build()
    }
}