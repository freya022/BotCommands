package com.freya02.botcommands.internal.commands.application.context.user.mixins

import com.freya02.botcommands.api.commands.application.slash.builder.mixins.ITopLevelApplicationCommandBuilder
import com.freya02.botcommands.internal.commands.application.mixins.TopLevelApplicationCommandInfoMixin

class TopLevelUserCommandInfoMixin(
    builder: ITopLevelApplicationCommandBuilder
) : TopLevelApplicationCommandInfoMixin(builder), ITopLevelUserCommandInfo