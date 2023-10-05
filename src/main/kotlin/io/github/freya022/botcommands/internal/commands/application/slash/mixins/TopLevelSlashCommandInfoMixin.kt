package io.github.freya022.botcommands.internal.commands.application.slash.mixins

import io.github.freya022.botcommands.api.commands.application.slash.builder.TopLevelSlashCommandBuilder
import io.github.freya022.botcommands.internal.commands.application.mixins.TopLevelApplicationCommandInfoMixin

class TopLevelSlashCommandInfoMixin(
    builder: TopLevelSlashCommandBuilder
) : TopLevelApplicationCommandInfoMixin(builder), ITopLevelSlashCommandInfo