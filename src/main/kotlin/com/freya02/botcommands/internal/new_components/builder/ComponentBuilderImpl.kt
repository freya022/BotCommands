package com.freya02.botcommands.internal.new_components.builder

import com.freya02.botcommands.api.components.InteractionConstraints
import com.freya02.botcommands.api.new_components.builder.ComponentBuilder
import com.freya02.botcommands.internal.new_components.ComponentHandler
import com.freya02.botcommands.internal.new_components.new.ComponentTimeout

@Suppress("UNCHECKED_CAST")
abstract class ComponentBuilderImpl<T: ComponentBuilder<T>> : ComponentBuilder<T> {
    final override var oneUse: Boolean = false
        private set
    final override var constraints: InteractionConstraints = InteractionConstraints()
        private set
    override val timeout: ComponentTimeout? = null
    override val handler: ComponentHandler? = null

    override fun oneUse(): T = this.also { oneUse = true } as T

    override fun constraints(block: InteractionConstraints.() -> Unit): T = this.also { constraints.apply(block) } as T
}