package com.freya02.botcommands.internal.commands.application

import com.freya02.botcommands.api.commands.application.builder.ApplicationCommandOptionAggregateBuilder
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.commands.CommandParameter

abstract class ApplicationCommandParameter(
    context: BContextImpl,
    optionAggregateBuilder: ApplicationCommandOptionAggregateBuilder<*>
) : CommandParameter(context, optionAggregateBuilder)