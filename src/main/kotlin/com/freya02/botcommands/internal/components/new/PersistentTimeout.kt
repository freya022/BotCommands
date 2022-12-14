package com.freya02.botcommands.internal.components.new

import kotlinx.datetime.Instant
import net.dv8tion.jda.api.entities.ISnowflake

class PersistentTimeout(
    override val expirationTimestamp: Instant,
    val handlerName: String?,
    userData: Array<out Any?>
) : ComponentTimeout {
    val userData: Array<out String> = processArgs(userData)

    private fun processArgs(args: Array<out Any?>): Array<out String> = args.map { arg ->
        when (arg) {
            is ISnowflake -> arg.id
            else -> arg.toString()
        }
    }.toTypedArray()
}