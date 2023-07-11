package com.freya02.botcommands.internal.commands.application.slash.mixins

import com.freya02.botcommands.api.commands.application.slash.builder.TopLevelSlashCommandBuilder
import com.freya02.botcommands.internal.commands.application.mixins.TopLevelApplicationCommandInfoMixin

class TopLevelSlashCommandInfoMixin(
    builder: TopLevelSlashCommandBuilder
) : TopLevelApplicationCommandInfoMixin(builder), ITopLevelSlashCommandInfo