package io.github.freya022.botcommands.internal.commands.text.builder

import io.github.freya022.botcommands.api.commands.INamedCommand
import io.github.freya022.botcommands.api.commands.text.builder.TextSubcommandBuilder
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.internal.commands.text.TextCommandInfoImpl
import io.github.freya022.botcommands.internal.commands.text.TextSubcommandInfoImpl

internal class TextSubcommandBuilderImpl internal constructor(
    context: BContext,
    name: String,
    override val parentInstance: INamedCommand,
) : TextCommandBuilderImpl(context, name),
    TextSubcommandBuilder {

    internal fun build(parentInstance: TextCommandInfoImpl): TextCommandInfoImpl {
        require(variations.isNotEmpty()) {
            "Text command should have at least 1 variation"
        }

        return TextSubcommandInfoImpl(context, this, parentInstance)
    }
}