package io.github.freya022.botcommands.internal.commands.text

import io.github.freya022.botcommands.api.commands.text.TextSubcommandInfo
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.internal.commands.text.builder.TextSubcommandBuilderImpl

internal class TextSubcommandInfoImpl(
    override val context: BContext,
    builder: TextSubcommandBuilderImpl,
    parentInstance: TextCommandInfoImpl
) : TextCommandInfoImpl(builder, parentInstance),
    TextSubcommandInfo