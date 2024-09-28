package io.github.freya022.botcommands.internal.components.builder

//See BuilderInstanceHolderImpl on how this is done internally
internal interface BuilderInstanceHolder<T : Any> {
    val instance: T
}
