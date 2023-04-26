package com.freya02.botcommands.internal.commands.application

import com.freya02.botcommands.api.commands.application.builder.ApplicationCommandOptionAggregateBuilder
import com.freya02.botcommands.internal.commands.CommandParameter
import kotlin.reflect.KParameter

abstract class ApplicationCommandParameter(
    parameter: KParameter,
    optionAggregateBuilder: ApplicationCommandOptionAggregateBuilder
) : CommandParameter(parameter, optionAggregateBuilder)