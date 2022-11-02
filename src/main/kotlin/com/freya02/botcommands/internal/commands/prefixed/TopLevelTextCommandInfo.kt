package com.freya02.botcommands.internal.commands.prefixed

import com.freya02.botcommands.api.commands.prefixed.builder.TopLevelTextCommandBuilder
import com.freya02.botcommands.internal.BContextImpl

class TopLevelTextCommandInfo(
    context: BContextImpl,
    builder: TopLevelTextCommandBuilder
) : TextCommandInfo(context, builder, null) {
    val category: String = builder.category
}