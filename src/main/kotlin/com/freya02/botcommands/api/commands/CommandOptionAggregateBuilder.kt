package com.freya02.botcommands.api.commands

import com.freya02.botcommands.api.core.options.builder.OptionAggregateBuilder
import com.freya02.botcommands.internal.parameters.AggregatorParameter
import kotlin.reflect.KFunction

abstract class CommandOptionAggregateBuilder(aggregatorParameter: AggregatorParameter, aggregator: KFunction<*>) : OptionAggregateBuilder(aggregatorParameter, aggregator)
