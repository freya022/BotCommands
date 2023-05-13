package com.freya02.botcommands.internal.commands.prefixed

import com.freya02.botcommands.api.commands.prefixed.builder.TextSubcommandBuilder
import com.freya02.botcommands.internal.commands.mixins.INamedCommand

class TextSubcommandInfo(
    builder: TextSubcommandBuilder,
    parentInstance: INamedCommand?
) : TextCommandInfo(builder, parentInstance)