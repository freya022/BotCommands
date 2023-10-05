package io.github.freya022.botcommands.internal.commands.application.context.message.mixins

import io.github.freya022.botcommands.api.commands.application.slash.builder.mixins.ITopLevelApplicationCommandBuilder
import io.github.freya022.botcommands.internal.commands.application.mixins.TopLevelApplicationCommandInfoMixin

class TopLevelMessageCommandInfoMixin(
    builder: ITopLevelApplicationCommandBuilder
) : TopLevelApplicationCommandInfoMixin(builder), ITopLevelMessageCommandInfo