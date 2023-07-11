package com.freya02.botcommands.internal.commands.application.context.message.mixins

import com.freya02.botcommands.api.commands.application.slash.builder.mixins.ITopLevelApplicationCommandBuilder
import com.freya02.botcommands.internal.commands.application.mixins.TopLevelApplicationCommandInfoMixin

class TopLevelMessageCommandInfoMixin(
    builder: ITopLevelApplicationCommandBuilder
) : TopLevelApplicationCommandInfoMixin(builder), ITopLevelMessageCommandInfo