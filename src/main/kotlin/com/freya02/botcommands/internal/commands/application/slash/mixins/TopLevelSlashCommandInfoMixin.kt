package com.freya02.botcommands.internal.commands.application.slash.mixins

import com.freya02.botcommands.api.commands.application.slash.builder.TopLevelSlashCommandBuilder
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.commands.application.mixins.TopLevelApplicationCommandInfoMixin

class TopLevelSlashCommandInfoMixin(
    context: BContextImpl,
    builder: TopLevelSlashCommandBuilder
) : TopLevelApplicationCommandInfoMixin(context, builder), ITopLevelSlashCommandInfo