package com.freya02.botcommands.internal

import com.freya02.botcommands.api.core.options.builder.OptionAggregateBuilder
import com.freya02.botcommands.internal.parameters.MethodParameter

@Deprecated("Will be replaced with a generic counterpart")
typealias MethodParameters = List<MethodParameter>

internal inline fun <reified T : OptionAggregateBuilder, R> Map<String, OptionAggregateBuilder>.transform(aggregateBlock: (T) -> R) =
    values.map {
        if (it !is T)
            throwInternal("Aggregates should have consisted of ${T::class.simpleNestedName} instances")
        aggregateBlock(it)
    }