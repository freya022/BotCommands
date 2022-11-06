package com.freya02.botcommands.internal.new_components

import net.dv8tion.jda.api.entities.ISnowflake
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent

internal interface ComponentHandler

internal class PersistentHandler(val handlerName: String, userData: Array<out Any?>) : ComponentHandler {
    val userData: Array<String>

    init {
        this.userData = processArgs(userData)
    }

    operator fun component1() = handlerName
    operator fun component2() = userData

    private fun processArgs(args: Array<out Any?>): Array<String> = args.map { arg ->
        when (arg) {
            is ISnowflake -> arg.id
            else -> arg.toString()
        }
    }.toTypedArray()
}

internal class EphemeralHandler<T : GenericComponentInteractionCreateEvent>(handler: (T) -> Unit) : ComponentHandler