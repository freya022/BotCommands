package com.freya02.botcommands.internal.new_components

import net.dv8tion.jda.api.entities.ISnowflake
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent

internal interface ComponentHandler

internal class PersistentHandler(val handlerName: String, args: Array<out Any?>) : ComponentHandler {
    val args: Array<out Any?>

    init {
        this.args = processArgs(args)
    }

    private fun processArgs(args: Array<out Any?>): Array<String> = args.map { arg ->
        when (arg) {
            is ISnowflake -> arg.id
            else -> arg.toString()
        }
    }.toTypedArray()
}

internal class EphemeralHandler<T : GenericComponentInteractionCreateEvent>(handler: (T) -> Unit) : ComponentHandler