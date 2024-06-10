package io.github.freya022.botcommands.internal.commands.text

import io.github.freya022.botcommands.api.commands.text.TextSubcommandInfo
import io.github.freya022.botcommands.api.commands.text.builder.TextSubcommandBuilder

internal class TextSubcommandInfoImpl(
    builder: TextSubcommandBuilder,
    parentInstance: TextCommandInfoImpl
) : TextCommandInfoImpl(builder, parentInstance),
    TextSubcommandInfo