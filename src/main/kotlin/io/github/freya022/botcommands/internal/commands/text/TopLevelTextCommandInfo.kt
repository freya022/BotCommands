package io.github.freya022.botcommands.internal.commands.text

import io.github.freya022.botcommands.api.commands.text.builder.TopLevelTextCommandBuilder

class TopLevelTextCommandInfo(
    builder: TopLevelTextCommandBuilder
) : TextCommandInfo(builder, null) {
    val category: String = builder.category
}