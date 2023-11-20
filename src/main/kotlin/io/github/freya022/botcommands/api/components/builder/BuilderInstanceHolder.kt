package io.github.freya022.botcommands.api.components.builder

import io.github.freya022.botcommands.internal.utils.throwInternal

interface BuilderInstanceHolder<T : BuilderInstanceHolder<T>> {
    val instance: T
        get() = throwInternal("This should have been overridden by the final type ${this.javaClass.name}")
}