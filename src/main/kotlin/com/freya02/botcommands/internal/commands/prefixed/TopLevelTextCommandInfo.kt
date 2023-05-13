package com.freya02.botcommands.internal.commands.prefixed

import com.freya02.botcommands.api.commands.prefixed.builder.TopLevelTextCommandBuilder

class TopLevelTextCommandInfo(
    builder: TopLevelTextCommandBuilder
) : TextCommandInfo(builder, null) {
    val category: String = builder.category
}