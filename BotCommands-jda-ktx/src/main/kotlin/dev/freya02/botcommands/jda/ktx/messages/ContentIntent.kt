package dev.freya02.botcommands.jda.ktx.messages

import net.dv8tion.jda.internal.entities.ReceivedMessage

/**
 * Temporarily suppresses message content intent warnings
 *
 * **Note:** This applies to all threads while the current code is inside this function
 */
inline fun <R> suppressContentWarning(block: () -> R): R {
    val oldFlag = ReceivedMessage.didContentIntentWarning
    ReceivedMessage.didContentIntentWarning = true

    return try {
        block()
    } finally {
        ReceivedMessage.didContentIntentWarning = oldFlag
    }
}
