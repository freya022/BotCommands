package com.freya02.botcommands.api.commands.application.slash.builder.mixins

import com.freya02.botcommands.api.commands.application.CommandScope

abstract class TopLevelApplicationCommandBuilderMixin(override val scope: CommandScope) : ITopLevelApplicationCommandBuilder {
    override val isDefaultLocked: Boolean = false
    override val isGuildOnly: Boolean = false
    override val isTestOnly: Boolean = false
}