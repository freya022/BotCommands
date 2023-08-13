package com.freya02.botcommands.internal.components

import com.freya02.botcommands.api.core.utils.simpleNestedName
import net.dv8tion.jda.api.entities.ISnowflake
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent

sealed interface ComponentHandler {
    val lifetimeType: LifetimeType
}

class PersistentHandler(val handlerName: String, userData: Array<out Any?>) : ComponentHandler {
    override val lifetimeType: LifetimeType = LifetimeType.PERSISTENT
    val userData: Array<out String> = processArgs(userData)

    operator fun component1() = handlerName
    operator fun component2() = userData

    override fun toString(): String {
        return "PersistentHandler(handlerName='$handlerName')"
    }

    private fun processArgs(args: Array<out Any?>): Array<String> = args.map { arg ->
        when (arg) {
            is ISnowflake -> arg.id
            else -> arg.toString()
        }
    }.toTypedArray()
}

class EphemeralHandler<T : GenericComponentInteractionCreateEvent>(val handler: suspend (T) -> Unit) : ComponentHandler {
    override val lifetimeType: LifetimeType = LifetimeType.EPHEMERAL

    override fun toString(): String {
        return "EphemeralHandler(handler=${handler::class.simpleNestedName})"
    }
}