package io.github.freya022.botcommands.api.commands.prefixed.builder

import io.github.freya022.botcommands.internal.commands.mixins.INamedCommand
import io.github.freya022.botcommands.internal.commands.prefixed.TopLevelTextCommandInfo
import io.github.freya022.botcommands.internal.core.BContextImpl

class TopLevelTextCommandBuilder internal constructor(context: BContextImpl, name: String) : TextCommandBuilder(context, name) {
    override val parentInstance: INamedCommand? = null

    var category: String = "No category"

    internal fun build(): TopLevelTextCommandInfo {
        require(variations.isNotEmpty() || subcommands.isNotEmpty()) {
            "Top-level-only text command should have at least 1 variation"
        }

        return TopLevelTextCommandInfo(this)
    }
}
