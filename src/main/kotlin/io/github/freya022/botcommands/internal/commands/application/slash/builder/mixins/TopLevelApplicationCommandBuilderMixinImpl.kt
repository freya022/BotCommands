package io.github.freya022.botcommands.internal.commands.application.slash.builder.mixins

import io.github.freya022.botcommands.api.commands.application.CommandScope

internal class TopLevelApplicationCommandBuilderMixinImpl internal constructor(
    override val scope: CommandScope,
) : TopLevelApplicationCommandBuilderMixin {

    override var isDefaultLocked: Boolean = false
    override var nsfw: Boolean = false
}