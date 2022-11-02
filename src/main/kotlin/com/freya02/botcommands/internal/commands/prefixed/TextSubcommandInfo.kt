package com.freya02.botcommands.internal.commands.prefixed

import com.freya02.botcommands.api.commands.prefixed.builder.TextSubcommandBuilder
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.commands.mixins.INamedCommandInfo

class TextSubcommandInfo(
    context: BContextImpl,
    builder: TextSubcommandBuilder,
    parentInstance: INamedCommandInfo?
) : TextCommandInfo(context, builder, parentInstance)