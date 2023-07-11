package com.freya02.botcommands.api.commands.application.slash.builder.mixins

import com.freya02.botcommands.api.commands.application.CommandScope

open class TopLevelApplicationCommandBuilderMixin(override val scope: CommandScope) : ITopLevelApplicationCommandBuilder {
    override var isDefaultLocked: Boolean = false
}