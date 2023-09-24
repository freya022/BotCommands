package com.freya02.botcommands.internal.components.builder

import com.freya02.botcommands.api.components.builder.IActionableComponent

internal sealed class AbstractActionableComponent : IActionableComponent {
    override var rateLimitGroup: String? = null

    override fun rateLimitReference(group: String) {
        this.rateLimitGroup = group
    }
}