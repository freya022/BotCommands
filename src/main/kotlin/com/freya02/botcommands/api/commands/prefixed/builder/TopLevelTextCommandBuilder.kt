package com.freya02.botcommands.api.commands.prefixed.builder

import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.commands.mixins.INamedCommand
import com.freya02.botcommands.internal.commands.prefixed.TopLevelTextCommandInfo

class TopLevelTextCommandBuilder internal constructor(context: BContextImpl, name: String) : TextCommandBuilder(context, name) {
    override val parentInstance: INamedCommand? = null

    var category: String = "No category"

    @JvmSynthetic
    internal fun build(): TopLevelTextCommandInfo {
        require(variations.isNotEmpty() || subcommands.isNotEmpty()) {
            "Top-level-only text command should have at least 1 variation"
        }

        return TopLevelTextCommandInfo(this)
    }
}
