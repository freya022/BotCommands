package com.freya02.botcommands.api.new_components.builder

import com.freya02.botcommands.api.components.InteractionConstraints

interface IConstrainableComponent {
    var constraints: InteractionConstraints

    fun constraints(block: InteractionConstraints.() -> Unit)
}