package io.github.freya022.botcommands.internal.commands.application.context.user.mixins

import io.github.freya022.botcommands.api.commands.application.slash.builder.mixins.ITopLevelApplicationCommandBuilder
import io.github.freya022.botcommands.internal.commands.application.mixins.TopLevelApplicationCommandInfoMixin

class TopLevelUserCommandInfoMixin(
    builder: ITopLevelApplicationCommandBuilder
) : TopLevelApplicationCommandInfoMixin(builder), ITopLevelUserCommandInfo