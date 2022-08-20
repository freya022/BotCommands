package com.freya02.botcommands.api.commands.application

import com.freya02.botcommands.api.commands.CommandPath
import com.freya02.botcommands.api.commands.application.context.builder.MessageCommandBuilder
import com.freya02.botcommands.api.commands.application.context.builder.UserCommandBuilder
import com.freya02.botcommands.api.commands.application.slash.builder.SlashCommandBuilder
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.commands.application.ApplicationCommandInfo
import com.freya02.botcommands.internal.throwUser
import net.dv8tion.jda.api.entities.Guild

class GuildApplicationCommandManager internal constructor(val context: BContextImpl, val guild: Guild): IApplicationCommandManager() {
    override val applicationCommands: MutableList<ApplicationCommandInfo> = arrayListOf()

    override fun slashCommand0(path: CommandPath, scope: CommandScope, builder: SlashCommandBuilder.() -> Unit) {
        if (scope.isGlobal) throwUser("You can only use non-global scopes in a GuildApplicationCommandManager")

        applicationCommands += SlashCommandBuilder(context, path, scope)
            .apply(builder)
            .build()
    }

    override fun userCommand0(name: String, scope: CommandScope, builder: UserCommandBuilder.() -> Unit) {
        if (scope.isGlobal) throwUser("You can only use non-global scopes in a GuildApplicationCommandManager")

        applicationCommands += UserCommandBuilder(context, CommandPath.ofName(name), scope)
            .apply(builder)
            .build()
    }

    override fun messageCommand0(name: String, scope: CommandScope, builder: MessageCommandBuilder.() -> Unit) {
        if (scope.isGlobal) throwUser("You can only use non-global scopes in a GuildApplicationCommandManager")

        applicationCommands += MessageCommandBuilder(context, CommandPath.ofName(name), scope)
            .apply(builder)
            .build()
    }
}