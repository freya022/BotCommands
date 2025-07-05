package io.github.freya022.botcommands.internal.core.hooks

import io.github.freya022.botcommands.api.core.annotations.BEventListener
import io.github.freya022.botcommands.internal.core.ClassPathFunction
import kotlin.time.Duration

internal class EventHandlerFunction(
    val classPathFunction: ClassPathFunction,
    val priority: Int,
    val runMode: BEventListener.RunMode,
    val timeout: Duration,
    private val parametersBlock: () -> Array<Any>
) {
    val parameters: Array<Any> by lazy {
        parametersBlock()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EventHandlerFunction

        return classPathFunction == other.classPathFunction
    }

    override fun hashCode(): Int {
        return classPathFunction.hashCode()
    }

    companion object {
        val priorityComparator: Comparator<EventHandlerFunction> = Comparator
            .comparingInt<EventHandlerFunction> { it.priority }
            .reversed()
    }
}