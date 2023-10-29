package io.github.freya022.botcommands.api.commands.text.builder

import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.internal.commands.mixins.INamedCommand
import io.github.freya022.botcommands.internal.commands.prefixed.TextCommandInfo
import io.github.freya022.botcommands.internal.commands.prefixed.TextSubcommandInfo

class TextSubcommandBuilder internal constructor(context: BContext, name: String,
                                                 override val parentInstance: INamedCommand) : TextCommandBuilder(context, name) {
    internal fun build(parentInstance: TextCommandInfo?): TextCommandInfo {
        require(variations.isNotEmpty()) {
            "Text command should have at least 1 variation"
        }

        return TextSubcommandInfo(this, parentInstance)
    }
}
