package io.github.freya022.botcommands.internal.components.handler

import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.internal.components.LifetimeType
import net.dv8tion.jda.api.entities.ISnowflake
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent

internal sealed interface ComponentHandler {
    val lifetimeType: LifetimeType
}

internal class PersistentHandler private constructor(val handlerName: String, val userData: List<String?>) : ComponentHandler {
    override val lifetimeType: LifetimeType = LifetimeType.PERSISTENT

    operator fun component1() = handlerName
    operator fun component2() = userData

    override fun toString(): String {
        return "PersistentHandler(handlerName='$handlerName')"
    }

    internal companion object {
        internal fun create(handlerName: String, userData: List<Any?>): PersistentHandler {
            return PersistentHandler(handlerName, processArgs(userData))
        }

        internal fun fromData(handlerName: String, userData: List<String?>): PersistentHandler {
            return PersistentHandler(handlerName, userData)
        }

        private fun processArgs(args: List<Any?>): List<String?> = args.map { arg ->
            when (arg) {
                null -> null
                is ISnowflake -> arg.id
                else -> arg.toString()
            }
        }
    }
}

internal class EphemeralHandler<T : GenericComponentInteractionCreateEvent> internal constructor(
    val handler: suspend (T) -> Unit
) : ComponentHandler {
    override val lifetimeType: LifetimeType = LifetimeType.EPHEMERAL

    override fun toString(): String {
        return "EphemeralHandler(handler=${handler::class.simpleNestedName})"
    }
}