package io.github.freya022.botcommands.internal.commands.text

import io.github.freya022.botcommands.api.commands.text.TopLevelTextCommandInfo
import io.github.freya022.botcommands.api.commands.text.builder.TopLevelTextCommandBuilder

internal class TopLevelTextCommandInfoImpl(
    builder: TopLevelTextCommandBuilder
) : TextCommandInfoImpl(builder, null),
    TopLevelTextCommandInfo {

    override val category: String = builder.category
}