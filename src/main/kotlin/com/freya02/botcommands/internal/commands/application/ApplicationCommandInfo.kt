package com.freya02.botcommands.internal.commands.application

import com.freya02.botcommands.api.commands.application.builder.ApplicationCommandBuilder
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.IExecutableInteractionInfo
import com.freya02.botcommands.internal.commands.AbstractCommandInfo
import com.freya02.botcommands.internal.commands.ExecutableInteractionInfo
import com.freya02.botcommands.internal.commands.ExecutableInteractionInfo.Companion.filterOptions
import com.freya02.botcommands.internal.commands.application.mixins.ITopLevelApplicationCommandInfo

abstract class ApplicationCommandInfo internal constructor(
    context: BContextImpl,
    builder: ApplicationCommandBuilder<*>
) : AbstractCommandInfo(builder), IExecutableInteractionInfo by ExecutableInteractionInfo(context, builder) {
    override val optionParameters: List<ApplicationCommandParameter> by lazy { parameters.filterOptions() }

    abstract val topLevelInstance: ITopLevelApplicationCommandInfo

    val nsfw: Boolean = builder.nsfw
}