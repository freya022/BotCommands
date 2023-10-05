package io.github.freya022.botcommands.api.commands.application.slash.builder.mixins

import io.github.freya022.botcommands.api.commands.application.CommandScope

class TopLevelSlashCommandBuilderMixin internal constructor(scope: CommandScope) : TopLevelApplicationCommandBuilderMixin(scope), ITopLevelSlashCommandBuilder