package dev.freya02.botcommands.jda.ktx.messages.deletes

import dev.freya02.botcommands.jda.ktx.DeprecatedInBcCore
import dev.freya02.botcommands.jda.ktx.durations.delay
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.requests.RestAction
import kotlin.time.Duration

/**
 * Deletes the original message using the hook after the specified delay.
 *
 * **Note:** This delays the rest action by the given delay.
 */
@DeprecatedInBcCore
fun <R> RestAction<R>.deleteDelayed(hook: InteractionHook, delay: Duration?): RestAction<R> {
    return withFiniteDelayOrSelf(delay) { finiteDelay ->
        delay(finiteDelay).flatMapOriginal { hook.deleteOriginal() }
    }
}

/**
 * Deletes the original message using the hook after the specified delay.
 *
 * **Note:** This delays the rest action by the given delay.
 */
@DeprecatedInBcCore
fun RestAction<InteractionHook>.deleteDelayed(delay: Duration?): RestAction<InteractionHook> {
    return withFiniteDelayOrSelf(delay) { finiteDelay ->
        delay(finiteDelay).flatMapOriginal(InteractionHook::deleteOriginal)
    }
}

/**
 * Deletes the message after the specified delay.
 *
 * **Note:** This delays the rest action by the given delay.
 */
@DeprecatedInBcCore
@JvmName("deleteDelayedMessage")
fun RestAction<Message>.deleteDelayed(delay: Duration?): RestAction<Message> {
    return withFiniteDelayOrSelf(delay) { finiteDelay ->
        delay(finiteDelay).flatMapOriginal(Message::delete)
    }
}

private inline fun <R : RestAction<*>> R.withFiniteDelayOrSelf(delay: Duration?, block: (finiteDelay: Duration) -> R): R {
    if (delay != null && delay.isFinite() && delay.isPositive())
        return block(delay)

    return this
}

private fun <T> RestAction<T>.flatMapOriginal(block: (T) -> RestAction<*>): RestAction<T> {
    return flatMap { original -> block(original).map { original } }
}
