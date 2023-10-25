package io.github.freya022.botcommands.internal.commands.application

import io.github.freya022.botcommands.api.commands.application.ApplicationCommandFilter
import io.github.freya022.botcommands.api.commands.application.builder.ApplicationCommandBuilder
import io.github.freya022.botcommands.internal.IExecutableInteractionInfo
import io.github.freya022.botcommands.internal.commands.AbstractCommandInfo
import io.github.freya022.botcommands.internal.commands.application.mixins.ITopLevelApplicationCommandInfo

abstract class ApplicationCommandInfo internal constructor(
    builder: ApplicationCommandBuilder<*>
) : AbstractCommandInfo(builder), IExecutableInteractionInfo {
    abstract val topLevelInstance: ITopLevelApplicationCommandInfo

    val filters: List<ApplicationCommandFilter<*>> = builder.filters
    val nsfw: Boolean = builder.nsfw
}