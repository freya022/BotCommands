package io.github.freya022.botcommands.api.commands.application.slash.builder.mixins

import io.github.freya022.botcommands.api.commands.application.CommandScope

open class TopLevelApplicationCommandBuilderMixin internal constructor(override val scope: CommandScope) : ITopLevelApplicationCommandBuilder {
    override var isDefaultLocked: Boolean = false
    override var nsfw: Boolean = false
}