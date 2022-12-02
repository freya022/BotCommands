package com.freya02.botcommands.internal.new_components.builder

import com.freya02.botcommands.api.new_components.builder.IUniqueComponent

internal class UniqueComponentImpl<T : IUniqueComponent<T>> : IUniqueComponent<T> {
    override var oneUse: Boolean = false

    //TODO prefer property accessors
    override fun oneUse(): T = this.also { oneUse = true } as T
}