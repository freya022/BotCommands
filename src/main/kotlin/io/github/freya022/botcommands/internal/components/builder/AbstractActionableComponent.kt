package io.github.freya022.botcommands.internal.components.builder

import io.github.freya022.botcommands.api.components.builder.IActionableComponent

internal sealed class AbstractActionableComponent : IActionableComponent {
    override var rateLimitGroup: String? = null

    override fun rateLimitReference(group: String) {
        this.rateLimitGroup = group
    }
}