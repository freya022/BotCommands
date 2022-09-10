package com.freya02.botcommands.api.commands.application.slash.builder.mixins

import com.freya02.botcommands.api.commands.application.CommandScope

class TopLevelSlashCommandBuilderMixin(scope: CommandScope) : TopLevelApplicationCommandBuilderMixin(scope), ITopLevelSlashCommandBuilder