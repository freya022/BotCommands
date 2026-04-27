package io.github.freya022.botcommands.internal.commands.text

import io.github.freya022.botcommands.api.commands.text.TopLevelTextCommandInfo
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.internal.commands.text.builder.TopLevelTextCommandBuilderImpl

internal class TopLevelTextCommandInfoImpl(
    override val context: BContext,
    builder: TopLevelTextCommandBuilderImpl
) : TextCommandInfoImpl(builder, null),
    TopLevelTextCommandInfo {

    override val category: String = builder.category
}