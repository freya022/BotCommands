package com.freya02.botcommands.internal.commands.application

import com.freya02.botcommands.api.commands.application.builder.ApplicationCommandBuilder
import com.freya02.botcommands.internal.IExecutableInteractionInfo
import com.freya02.botcommands.internal.commands.AbstractCommandInfo
import com.freya02.botcommands.internal.commands.application.mixins.ITopLevelApplicationCommandInfo

abstract class ApplicationCommandInfo internal constructor(
    builder: ApplicationCommandBuilder<*>
) : AbstractCommandInfo(builder), IExecutableInteractionInfo {
    abstract val topLevelInstance: ITopLevelApplicationCommandInfo

    val nsfw: Boolean = builder.nsfw
}