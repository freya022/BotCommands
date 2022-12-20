package com.freya02.botcommands.api.commands.prefixed.builder

import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.commands.mixins.INamedCommandInfo
import com.freya02.botcommands.internal.commands.prefixed.TopLevelTextCommandInfo

class TopLevelTextCommandBuilder internal constructor(context: BContextImpl, name: String) : TextCommandBuilder(context, name) {
    override val parentInstance: INamedCommandInfo? = null

    var category: String = "No category"

    @JvmSynthetic
    internal fun build(): TopLevelTextCommandInfo {
        require(variations.isNotEmpty()) {
            "Text command should have at least 1 variation"
        }

        return TopLevelTextCommandInfo(context, this)
    }
}
