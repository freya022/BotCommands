package com.freya02.botcommands.internal.new_components.builder.select

import com.freya02.botcommands.api.new_components.builder.select.SelectBuilder
import com.freya02.botcommands.internal.new_components.ComponentType
import com.freya02.botcommands.internal.new_components.builder.ComponentBuilderImpl

internal abstract class SelectBuilderImpl<T : SelectBuilder<T>> : ComponentBuilderImpl<T>(), SelectBuilder<T> {
    final override val componentType: ComponentType = ComponentType.SELECT_MENU
}