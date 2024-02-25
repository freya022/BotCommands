package io.github.freya022.botcommands.api.commands.application.provider

import io.github.freya022.botcommands.api.commands.application.CommandScope
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.internal.utils.throwUser
import net.dv8tion.jda.api.entities.Guild

class GuildApplicationCommandManager internal constructor(context: BContext, val guild: Guild): AbstractApplicationCommandManager(context) {
    override val defaultScope = CommandScope.GUILD

    override fun isValidScope(scope: CommandScope): Boolean = !scope.isGlobal

    override fun checkScope(scope: CommandScope) {
        if (!isValidScope(scope)) throwUser("You can only use non-global scopes in a GuildApplicationCommandManager")
    }
}