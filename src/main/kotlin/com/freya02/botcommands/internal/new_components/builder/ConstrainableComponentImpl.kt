package com.freya02.botcommands.internal.new_components.builder

import com.freya02.botcommands.api.components.InteractionConstraints
import com.freya02.botcommands.api.new_components.builder.IConstrainableComponent

internal class ConstrainableComponentImpl : IConstrainableComponent {
    override var constraints: InteractionConstraints = InteractionConstraints()

    override fun constraints(block: InteractionConstraints.() -> Unit) {
        constraints.apply(block)
    }
}