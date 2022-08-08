package com.freya02.botcommands.api.application

import com.freya02.botcommands.api.application.builder.MessageCommandBuilder
import com.freya02.botcommands.api.application.builder.SlashCommandBuilder
import com.freya02.botcommands.api.application.builder.UserCommandBuilder
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.application.ApplicationCommandInfo
import com.freya02.botcommands.internal.throwUser
import net.dv8tion.jda.api.entities.Guild

class GuildApplicationCommandManager internal constructor(val context: BContextImpl, val guild: Guild): IApplicationCommandManager() {
    override val guildApplicationCommands: MutableList<ApplicationCommandInfo> = arrayListOf()

    override fun slashCommand0(path: CommandPath, scope: CommandScope, builder: SlashCommandBuilder.() -> Unit) {
        if (scope.isGlobal) throwUser("You can only use non-global scopes in a GuildApplicationCommandManager")

        guildApplicationCommands += SlashCommandBuilder(context, path, scope)
            .apply(builder)
            .build()
    }

    override fun userCommand0(name: String, scope: CommandScope, builder: UserCommandBuilder.() -> Unit) {
        if (scope.isGlobal) throwUser("You can only use non-global scopes in a GuildApplicationCommandManager")

        guildApplicationCommands += UserCommandBuilder(context, CommandPath.ofName(name), scope)
            .apply(builder)
            .build()
    }

    override fun messageCommand0(name: String, scope: CommandScope, builder: MessageCommandBuilder.() -> Unit) {
        if (scope.isGlobal) throwUser("You can only use non-global scopes in a GuildApplicationCommandManager")

        guildApplicationCommands += MessageCommandBuilder(context, CommandPath.ofName(name), scope)
            .apply(builder)
            .build()
    }
}