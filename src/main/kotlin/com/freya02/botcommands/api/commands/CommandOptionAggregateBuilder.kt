package com.freya02.botcommands.api.commands

import com.freya02.botcommands.api.core.options.builder.OptionAggregateBuilder
import com.freya02.botcommands.internal.parameters.MultiParameter
import kotlin.reflect.KFunction

abstract class CommandOptionAggregateBuilder(multiParameter: MultiParameter, aggregator: KFunction<*>) : OptionAggregateBuilder(multiParameter, aggregator)
