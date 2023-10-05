package com.freya02.botcommands.internal.commands.application

import com.freya02.botcommands.api.commands.application.builder.ApplicationCommandOptionAggregateBuilder
import com.freya02.botcommands.internal.commands.CommandParameter
import com.freya02.botcommands.internal.core.BContextImpl

abstract class ApplicationCommandParameter(
    context: BContextImpl,
    optionAggregateBuilder: ApplicationCommandOptionAggregateBuilder<*>
) : CommandParameter(context, optionAggregateBuilder)