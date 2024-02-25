package io.github.freya022.botcommands.api.commands.application.provider

import io.github.freya022.botcommands.api.commands.application.CommandScope
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.internal.utils.throwUser

class GlobalApplicationCommandManager internal constructor(context: BContext): AbstractApplicationCommandManager(context) {
    override val defaultScope = CommandScope.GLOBAL_NO_DM

    override fun isValidScope(scope: CommandScope) = scope.isGlobal

    override fun checkScope(scope: CommandScope) {
        if (!isValidScope(scope)) throwUser("You can only use global scopes in a GlobalApplicationCommandManager")
    }
}