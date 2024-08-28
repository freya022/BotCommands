package io.github.freya022.botcommands.internal.components.data.timeout

import net.dv8tion.jda.api.entities.ISnowflake

internal class PersistentTimeout private constructor(
    val handlerName: String,
    val userData: List<String?>
) : ComponentTimeout {
    internal companion object {
        internal fun create(handlerName: String, userData: List<Any?>): PersistentTimeout {
            return PersistentTimeout(
                handlerName,
                userData.map { arg ->
                    when (arg) {
                        null -> null
                        is ISnowflake -> arg.id
                        else -> arg.toString()
                    }
                }
            )
        }

        internal fun fromData(handlerName: String, userData: List<String>): PersistentTimeout {
            return PersistentTimeout(
                handlerName,
                userData
            )
        }
    }
}