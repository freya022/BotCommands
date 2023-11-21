package io.github.freya022.botcommands.api.components.builder

//See BuilderInstanceHolderImpl on how this is done internally
interface BuilderInstanceHolder<T : BuilderInstanceHolder<T>> {
    val instance: T
}