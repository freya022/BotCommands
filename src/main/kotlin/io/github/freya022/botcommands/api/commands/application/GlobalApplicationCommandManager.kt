package io.github.freya022.botcommands.api.commands.application

import io.github.freya022.botcommands.internal.core.BContextImpl
import io.github.freya022.botcommands.internal.utils.throwUser

class GlobalApplicationCommandManager internal constructor(context: BContextImpl): AbstractApplicationCommandManager(context) {
    override fun isValidScope(scope: CommandScope) = scope.isGlobal

    override fun checkScope(scope: CommandScope) {
        if (!isValidScope(scope)) throwUser("You can only use global scopes in a GlobalApplicationCommandManager")
    }
}