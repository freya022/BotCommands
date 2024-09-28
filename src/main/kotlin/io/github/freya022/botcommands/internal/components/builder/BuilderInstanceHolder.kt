package io.github.freya022.botcommands.internal.components.builder

//See BuilderInstanceHolderImpl on how this is done internally
internal interface BuilderInstanceHolder<T : Any> {
    val instance: T
}

internal inline fun <T : Any> BuilderInstanceHolder<T>.applyInstance(block: () -> Unit): T {
    block() // No need to give the instance
    return instance
}