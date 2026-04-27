package io.github.freya022.botcommands.internal.commands.text.builder

import io.github.freya022.botcommands.api.commands.INamedCommand
import io.github.freya022.botcommands.api.commands.text.builder.TopLevelTextCommandBuilder
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.internal.commands.text.TopLevelTextCommandInfoImpl

internal class TopLevelTextCommandBuilderImpl internal constructor(
    context: BContext,
    name: String,
) : TextCommandBuilderImpl(context, name),
    TopLevelTextCommandBuilder {

    override val parentInstance: INamedCommand? get() = null

    override var category: String = "No category"

    internal fun build(): TopLevelTextCommandInfoImpl {
        require(variations.isNotEmpty() || subcommands.isNotEmpty()) {
            "Top-level-only text command should have at least 1 variation"
        }

        return TopLevelTextCommandInfoImpl(context, this)
    }
}