package io.github.freya022.botcommands.internal.components.data

import io.github.freya022.botcommands.api.components.data.ComponentTimeout
import kotlinx.datetime.Instant
import kotlinx.datetime.toKotlinInstant
import net.dv8tion.jda.api.entities.ISnowflake
import java.sql.Timestamp as SQLTimestamp

internal class PersistentTimeout private constructor(
    override val expirationTimestamp: Instant,
    val handlerName: String?,
    val userData: List<String?>
) : ComponentTimeout {
    internal companion object {
        internal fun create(expirationTimestamp: Instant): PersistentTimeout {
            return PersistentTimeout(
                expirationTimestamp,
                null,
                emptyList()
            )
        }

        internal fun create(expirationTimestamp: Instant, handlerName: String?, userData: List<Any?>): PersistentTimeout {
            return PersistentTimeout(
                expirationTimestamp,
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

        internal fun fromData(expirationTimestamp: SQLTimestamp, handlerName: String?, userData: List<String>): PersistentTimeout {
            return PersistentTimeout(
                expirationTimestamp.toInstant().toKotlinInstant(),
                handlerName,
                userData
            )
        }
    }
}