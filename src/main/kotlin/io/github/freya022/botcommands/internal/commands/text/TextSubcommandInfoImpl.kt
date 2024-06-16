package io.github.freya022.botcommands.internal.commands.text

import io.github.freya022.botcommands.api.commands.text.TextSubcommandInfo
import io.github.freya022.botcommands.api.commands.text.builder.TextSubcommandBuilder
import io.github.freya022.botcommands.api.core.BContext

internal class TextSubcommandInfoImpl(
    override val context: BContext,
    builder: TextSubcommandBuilder,
    parentInstance: TextCommandInfoImpl
) : TextCommandInfoImpl(builder, parentInstance),
    TextSubcommandInfo