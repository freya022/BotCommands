package com.freya02.botcommands.api.new_components.builder

interface IPersistentActionableComponent<T : IPersistentActionableComponent<T>> {
    fun bindTo(handlerName: String, vararg data: Any?): T
}