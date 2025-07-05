package io.github.freya022.botcommands.api.components.builder.group

import io.github.freya022.botcommands.api.components.ComponentGroup
import io.github.freya022.botcommands.api.components.builder.IComponentBuilder
import io.github.freya022.botcommands.api.components.builder.ITimeoutableComponent

interface ComponentGroupBuilder<T : ComponentGroupBuilder<T>> :
        IComponentBuilder,
        ITimeoutableComponent<T> {

    fun build(): ComponentGroup
}