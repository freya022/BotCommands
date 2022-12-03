package com.freya02.botcommands.api.new_components.builder

import com.freya02.botcommands.api.components.InteractionConstraints

abstract class AbstractComponentBuilder internal constructor() : ComponentBuilder {
    final override var oneUse: Boolean = false
    final override var constraints: InteractionConstraints = InteractionConstraints()

    fun oneUse() = this.also { oneUse = true }

    override fun constraints(block: InteractionConstraints.() -> Unit) { constraints.apply(block) }
}