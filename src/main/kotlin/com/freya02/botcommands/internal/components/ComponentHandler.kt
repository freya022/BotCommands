package com.freya02.botcommands.internal.components

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

    private fun processArgs(args: Array<out Any?>): Array<String> = args.map { arg ->
        when (arg) {
            is ISnowflake -> arg.id
            else -> arg.toString()
        }
    }.toTypedArray()
}

class EphemeralHandler<T : GenericComponentInteractionCreateEvent>(val handler: suspend (T) -> Unit) : ComponentHandler {
    override val lifetimeType: LifetimeType = LifetimeType.EPHEMERAL
}