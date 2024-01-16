package io.github.freya022.botcommands.api.commands.text.builder

import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.internal.commands.mixins.INamedCommand
import io.github.freya022.botcommands.internal.commands.text.TopLevelTextCommandInfo

class TopLevelTextCommandBuilder internal constructor(context: BContext, name: String) : TextCommandBuilder(context, name) {
    override val parentInstance: INamedCommand? = null

    var category: String = "No category"

    internal fun build(): TopLevelTextCommandInfo {
        require(variations.isNotEmpty() || subcommands.isNotEmpty()) {
            "Top-level-only text command should have at least 1 variation"
        }

        return TopLevelTextCommandInfo(this)
    }
}
