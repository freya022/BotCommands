package com.freya02.botcommands.internal.commands.application

import com.freya02.botcommands.api.commands.application.builder.ApplicationCommandBuilder
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.IExecutableInteractionInfo
import com.freya02.botcommands.internal.commands.AbstractCommandInfo
import com.freya02.botcommands.internal.commands.ExecutableInteractionInfo
import com.freya02.botcommands.internal.commands.application.mixins.ITopLevelApplicationCommandInfo

abstract class ApplicationCommandInfo internal constructor(
    context: BContextImpl,
    builder: ApplicationCommandBuilder
) : AbstractCommandInfo(context, builder), IExecutableInteractionInfo by ExecutableInteractionInfo(context, builder) {
    @Suppress("UNCHECKED_CAST")
    override val optionParameters: List<ApplicationCommandParameter>
        get() = super.optionParameters as List<ApplicationCommandParameter>

    abstract val topLevelInstance: ITopLevelApplicationCommandInfo
}