package com.freya02.botcommands.internal.new_components.builder

import com.freya02.botcommands.api.components.InteractionConstraints
import com.freya02.botcommands.api.new_components.builder.ComponentBuilder

@Suppress("UNCHECKED_CAST")
abstract class ComponentBuilderImpl<T: ComponentBuilder<T>> : ComponentBuilder<T> {
    final override var oneUse: Boolean = false
        private set
    final override var constraints: InteractionConstraints = InteractionConstraints()
        private set

    override fun oneUse(): T = this.also { oneUse = true } as T

    override fun constraints(block: InteractionConstraints.() -> Unit): T = this.also { constraints.apply(block) } as T

    override fun setConstraints(constraints: InteractionConstraints): T = this.also { this.constraints = constraints } as T
}