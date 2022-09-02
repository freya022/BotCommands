package com.freya02.botcommands.internal.commands.application.context.message.mixins

import com.freya02.botcommands.api.commands.application.slash.builder.mixins.ITopLevelApplicationCommandBuilder
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.commands.application.slash.mixins.TopLevelApplicationCommandInfoMixin

class TopLevelMessageCommandInfoMixin(
    context: BContextImpl,
    builder: ITopLevelApplicationCommandBuilder
) : TopLevelApplicationCommandInfoMixin(context, builder), ITopLevelMessageCommandInfo