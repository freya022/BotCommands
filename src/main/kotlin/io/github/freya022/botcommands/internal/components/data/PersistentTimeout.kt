package io.github.freya022.botcommands.internal.components.data

import io.github.freya022.botcommands.api.components.data.ComponentTimeout
import kotlinx.datetime.Instant
import net.dv8tion.jda.api.entities.ISnowflake

internal class PersistentTimeout(
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