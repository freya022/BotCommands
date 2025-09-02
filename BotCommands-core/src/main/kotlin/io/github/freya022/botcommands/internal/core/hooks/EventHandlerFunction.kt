package io.github.freya022.botcommands.internal.core.hooks

import dev.freya02.botcommands.method.accessors.internal.MethodArguments
import io.github.freya022.botcommands.api.core.annotations.BEventListener
import io.github.freya022.botcommands.internal.core.ClassPathFunction
import kotlin.time.Duration

internal class EventHandlerFunction(
    val classPathFunction: ClassPathFunction,
    val priority: Int,
    val runMode: BEventListener.RunMode,
    val timeout: Duration?,
    private val parametersBlock: () -> Array<Any>
) {
    private val baseArgs: MethodArguments by lazy {
        val args = classPathFunction.methodAccessor.createBlankArguments()
        parametersBlock().forEachIndexed { index, arg ->
            // +1 as the first parameter is the event
            args[index + 1] = arg
        }
        args
    }

    // Since the arguments are the same everytime except for the event,
    // clone and only change the event on each invocation
    internal fun cloneBaseArgs(): MethodArguments = baseArgs.clone()

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
