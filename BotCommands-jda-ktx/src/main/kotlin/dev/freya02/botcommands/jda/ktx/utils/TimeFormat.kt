package dev.freya02.botcommands.jda.ktx.utils

import net.dv8tion.jda.api.utils.TimeFormat
import net.dv8tion.jda.api.utils.Timestamp
import kotlin.time.Instant
import kotlin.time.toJavaInstant

/**
 * Converts the provided [Instant] into a [Timestamp] with this style.
 *
 * @param  instant
 *         The [Instant] for the timestamp
 *
 * @return The [Timestamp] instance
 *
 * @see    TimeFormat.now
 * @see    TimeFormat.atTimestamp
 */
fun TimeFormat.atInstant(instant: Instant) = atInstant(instant.toJavaInstant())
