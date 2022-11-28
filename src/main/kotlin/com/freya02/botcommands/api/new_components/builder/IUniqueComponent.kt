package com.freya02.botcommands.api.new_components.builder

interface IUniqueComponent<T> {
    val oneUse: Boolean

    fun oneUse(): T
}