package dev.freya02.botcommands.jda.ktx

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ThreadContextElement
import kotlinx.coroutines.withContext
import net.dv8tion.jda.api.audit.ThreadLocalReason
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

/**
 * [ThreadLocalReason] context element for [CoroutineContext].
 *
 * @see withRestActionReasonContext
 */
class RestActionReasonContext(
    private val reason: String,
) : AbstractCoroutineContextElement(Key), ThreadContextElement<String?> {
    companion object Key : CoroutineContext.Key<RestActionReasonContext>

    override fun updateThreadContext(context: CoroutineContext): String? {
        val oldState = ThreadLocalReason.getCurrent()
        ThreadLocalReason.setCurrent(reason)
        return oldState
    }

    override fun restoreThreadContext(context: CoroutineContext, oldState: String?) {
        ThreadLocalReason.setCurrent(oldState)
    }

    override fun toString(): String = "RestActionReasonContext(reason=$reason)"
}

/**
 * Applies the provided reason as the default reason for all [AuditableRestAction] in the block.
 *
 * This is the equivalent of [ThreadLocalReason] for coroutines.
 *
 * **Note:** If you want to modify the reason inside the block, use this function again,
 * do **not** use [ThreadLocalReason.setCurrent].
 *
 * @see RestActionReasonContext
 */
suspend fun <R> withRestActionReasonContext(reason: String, block: suspend CoroutineScope.() -> R): R {
    return withContext(RestActionReasonContext(reason), block)
}
