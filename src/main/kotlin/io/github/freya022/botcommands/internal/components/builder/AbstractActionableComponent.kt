package io.github.freya022.botcommands.internal.components.builder

import io.github.freya022.botcommands.api.components.ComponentInteractionFilter
import io.github.freya022.botcommands.api.components.builder.IActionableComponent
import io.github.freya022.botcommands.api.core.BContext

internal sealed class AbstractActionableComponent(override val context: BContext) : IActionableComponent {
    override val filters: MutableList<ComponentInteractionFilter<*>> = arrayListOf()

    override var rateLimitGroup: String? = null

    override fun rateLimitReference(group: String) {
        this.rateLimitGroup = group
    }
}