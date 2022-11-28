package com.freya02.botcommands.api.new_components.builder

interface PersistentButtonBuilder : ButtonBuilder<PersistentButtonBuilder>, IPersistentTimeoutableComponent<PersistentButtonBuilder> {
    fun bindTo(handlerName: String, vararg data: Any?): PersistentButtonBuilder
}