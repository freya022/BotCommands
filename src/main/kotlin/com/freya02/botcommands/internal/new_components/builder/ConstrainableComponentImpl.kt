package com.freya02.botcommands.internal.new_components.builder

import com.freya02.botcommands.api.components.InteractionConstraints
import com.freya02.botcommands.api.new_components.builder.IConstrainableComponent

internal class ConstrainableComponentImpl<T : IConstrainableComponent<T>> : IConstrainableComponent<T> {
    override var constraints: InteractionConstraints = InteractionConstraints()

    override fun constraints(block: InteractionConstraints.() -> Unit): T = this.also { constraints.apply(block) } as T

    //TODO prefer property accessors
    override fun setConstraints(constraints: InteractionConstraints): T = this.also { this.constraints = constraints } as T
}