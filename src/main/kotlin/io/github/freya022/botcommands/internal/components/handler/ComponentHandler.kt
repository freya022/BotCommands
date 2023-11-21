package io.github.freya022.botcommands.internal.components.handler

import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.internal.components.LifetimeType
import net.dv8tion.jda.api.entities.ISnowflake
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent

sealed interface ComponentHandler {
    val lifetimeType: LifetimeType
}

class PersistentHandler(val handlerName: String, userData: List<Any?>) : ComponentHandler {
    override val lifetimeType: LifetimeType = LifetimeType.PERSISTENT
    val userData: List<String?> = processArgs(userData)

    operator fun component1() = handlerName
    operator fun component2() = userData

    override fun toString(): String {
        return "PersistentHandler(handlerName='$handlerName')"
    }

    private fun processArgs(args: List<Any?>): List<String?> = args.map { arg ->
        when (arg) {
            null -> null
            is ISnowflake -> arg.id
            else -> arg.toString()
        }
    }
}

class EphemeralHandler<T : GenericComponentInteractionCreateEvent>(val handler: suspend (T) -> Unit) :
    ComponentHandler {
    override val lifetimeType: LifetimeType = LifetimeType.EPHEMERAL

    override fun toString(): String {
        return "EphemeralHandler(handler=${handler::class.simpleNestedName})"
    }
}