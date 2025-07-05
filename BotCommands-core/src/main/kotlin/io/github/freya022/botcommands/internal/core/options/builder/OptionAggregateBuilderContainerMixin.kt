package io.github.freya022.botcommands.internal.core.options.builder

import io.github.freya022.botcommands.api.core.options.builder.OptionAggregateBuilder
import io.github.freya022.botcommands.api.core.options.builder.OptionAggregateBuilderContainer

internal interface OptionAggregateBuilderContainerMixin<T : OptionAggregateBuilder<T>> : OptionAggregateBuilderContainer<T> {
    val optionAggregateBuilders: Map<String, T>

    fun hasVararg(): Boolean

    fun selfAggregate(declaredName: String, block: T.() -> Unit)

    fun varargAggregate(declaredName: String, block: T.() -> Unit)
}