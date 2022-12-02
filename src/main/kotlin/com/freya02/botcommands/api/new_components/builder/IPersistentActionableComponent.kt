package com.freya02.botcommands.api.new_components.builder

interface IPersistentActionableComponent<T : IPersistentActionableComponent<T>> : IActionableComponent {
    fun bindTo(handlerName: String, vararg data: Any?): T
}