package com.freya02.botcommands.internal.commands.application

import com.freya02.botcommands.api.commands.application.builder.ApplicationCommandBuilder
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.MethodParameters
import com.freya02.botcommands.internal.commands.AbstractCommandInfo
import com.freya02.botcommands.internal.parameters.MethodParameter

abstract class ApplicationCommandInfo internal constructor(
    context: BContextImpl,
    builder: ApplicationCommandBuilder
) : AbstractCommandInfo(context, builder) {
    abstract override val parameters: MethodParameters
    override val optionParameters: List<MethodParameter>
        get() = super.optionParameters
}