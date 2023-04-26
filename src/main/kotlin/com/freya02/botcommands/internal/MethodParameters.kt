package com.freya02.botcommands.internal

import com.freya02.botcommands.api.core.options.builder.OptionAggregateBuilder
import com.freya02.botcommands.internal.parameters.MethodParameter

class MethodParameters internal constructor(
    methodParameters: List<MethodParameter>
) : ArrayList<MethodParameter>(methodParameters) {
    companion object {
        internal inline fun <reified T : OptionAggregateBuilder> transform(
            options: Map<String, OptionAggregateBuilder> = mapOf(),
            aggregateBlock: (T) -> MethodParameter
        ) = MethodParameters(options.values.map {
            if (it !is T)
                throwInternal("Aggregates should have consisted of ${T::class.simpleNestedName} instances")
            aggregateBlock(it)
        })
    }
}