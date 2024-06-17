package io.github.freya022.botcommands.api.commands.application.provider

import io.github.freya022.botcommands.api.commands.application.CommandScope
import io.github.freya022.botcommands.api.core.BContext

class GlobalApplicationCommandManager internal constructor(context: BContext): AbstractApplicationCommandManager(context) {
    override val defaultScope = CommandScope.GLOBAL_NO_DM

    override fun isValidScope(scope: CommandScope) = scope.isGlobal

    override fun checkScope(scope: CommandScope) {
        require(isValidScope(scope)) {
            "You can only use global scopes in a GlobalApplicationCommandManager"
        }
    }
}