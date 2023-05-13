package com.freya02.botcommands.api.commands.prefixed.builder

import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.commands.mixins.INamedCommand
import com.freya02.botcommands.internal.commands.prefixed.TextCommandInfo
import com.freya02.botcommands.internal.commands.prefixed.TextSubcommandInfo

class TextSubcommandBuilder internal constructor(context: BContextImpl, name: String,
                                                 override val parentInstance: INamedCommand) : TextCommandBuilder(context, name) {
    @JvmSynthetic
    internal fun build(parentInstance: TextCommandInfo?): TextCommandInfo {
        require(variations.isNotEmpty()) {
            "Text command should have at least 1 variation"
        }

        return TextSubcommandInfo(this, parentInstance)
    }
}
