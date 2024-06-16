package io.github.freya022.botcommands.api.commands.text.builder

import io.github.freya022.botcommands.api.commands.INamedCommand
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.internal.commands.text.TextCommandInfoImpl
import io.github.freya022.botcommands.internal.commands.text.TextSubcommandInfoImpl

class TextSubcommandBuilder internal constructor(
    context: BContext,
    name: String,
    override val parentInstance: INamedCommand,
) : TextCommandBuilder(context, name) {
    internal fun build(parentInstance: TextCommandInfoImpl): TextCommandInfoImpl {
        require(variations.isNotEmpty()) {
            "Text command should have at least 1 variation"
        }

        return TextSubcommandInfoImpl(context, this, parentInstance)
    }
}
