package io.github.freya022.botcommands.internal.commands.prefixed

import io.github.freya022.botcommands.api.commands.text.builder.TextSubcommandBuilder
import io.github.freya022.botcommands.internal.commands.mixins.INamedCommand

class TextSubcommandInfo(
    builder: TextSubcommandBuilder,
    parentInstance: INamedCommand?
) : TextCommandInfo(builder, parentInstance)