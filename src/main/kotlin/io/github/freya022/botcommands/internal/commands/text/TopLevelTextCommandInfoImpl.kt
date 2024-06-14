package io.github.freya022.botcommands.internal.commands.text

import io.github.freya022.botcommands.api.commands.text.TopLevelTextCommandInfo
import io.github.freya022.botcommands.api.commands.text.builder.TopLevelTextCommandBuilder
import io.github.freya022.botcommands.api.core.BContext

internal class TopLevelTextCommandInfoImpl(
    override val context: BContext,
    builder: TopLevelTextCommandBuilder
) : TextCommandInfoImpl(builder, null),
    TopLevelTextCommandInfo {

    override val category: String = builder.category
}