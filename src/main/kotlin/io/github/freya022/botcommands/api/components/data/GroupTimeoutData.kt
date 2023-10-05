package io.github.freya022.botcommands.api.components.data

data class GroupTimeoutData internal constructor(private val _componentIds: List<Int>) {
    val componentIds: List<String> by lazy {
        _componentIds.map { it.toString() }
    }
}