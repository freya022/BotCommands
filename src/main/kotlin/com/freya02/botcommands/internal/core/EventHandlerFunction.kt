package com.freya02.botcommands.internal.core

import kotlin.time.Duration

internal class EventHandlerFunction(
    val classPathFunction: ClassPathFunction,
    val priority: Int,
    val isAsync: Boolean,
    val timeout: Duration,
    private val parametersBlock: () -> Array<Any>
) : Comparable<EventHandlerFunction> {
    val parameters: Array<Any> by lazy {
        parametersBlock()
    }

    //Higher priority is above
    override fun compareTo(other: EventHandlerFunction) = -priority.compareTo(other.priority)
}
