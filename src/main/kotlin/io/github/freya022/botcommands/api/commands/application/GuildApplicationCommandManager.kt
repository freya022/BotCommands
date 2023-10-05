package io.github.freya022.botcommands.api.commands.application

import io.github.freya022.botcommands.internal.core.BContextImpl
import io.github.freya022.botcommands.internal.utils.throwUser
import net.dv8tion.jda.api.entities.Guild

class GuildApplicationCommandManager internal constructor(context: BContextImpl, val guild: Guild): AbstractApplicationCommandManager(context) {
    override fun isValidScope(scope: CommandScope): Boolean = !scope.isGlobal

    override fun checkScope(scope: CommandScope) {
        if (!isValidScope(scope)) throwUser("You can only use non-global scopes in a GuildApplicationCommandManager")
    }
}