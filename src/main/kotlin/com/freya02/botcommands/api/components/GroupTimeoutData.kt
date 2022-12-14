package com.freya02.botcommands.api.components

data class GroupTimeoutData(private val _componentIds: List<Int>) {
    val componentIds: List<String> by lazy {
        _componentIds.map { it.toString() }
    }
}