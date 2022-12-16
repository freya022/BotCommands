package com.freya02.botcommands.internal.components.builder

import com.freya02.botcommands.api.components.builder.IUniqueComponent

internal class UniqueComponentImpl : IUniqueComponent {
    override var oneUse: Boolean = false
}