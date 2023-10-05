package io.github.freya022.botcommands.internal.components.builder

import io.github.freya022.botcommands.api.components.builder.IUniqueComponent

internal class UniqueComponentImpl : IUniqueComponent {
    override var oneUse: Boolean = false
}