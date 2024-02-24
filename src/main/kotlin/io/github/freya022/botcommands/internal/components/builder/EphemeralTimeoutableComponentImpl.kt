package io.github.freya022.botcommands.internal.components.builder

import io.github.freya022.botcommands.api.components.Components
import io.github.freya022.botcommands.api.components.builder.IEphemeralTimeoutableComponent
import io.github.freya022.botcommands.internal.components.data.EphemeralTimeout
import io.github.freya022.botcommands.internal.utils.throwUser
import io.github.freya022.botcommands.internal.utils.toTimestampIfFinite
import kotlin.time.Duration

internal class EphemeralTimeoutableComponentImpl<T : IEphemeralTimeoutableComponent<T>> internal constructor(
    override val instanceRetriever: InstanceRetriever<T>
) : BuilderInstanceHolderImpl<T>(),
    IEphemeralTimeoutableComponent<T> {
    override var timeout: EphemeralTimeout? = Components.defaultTimeout.toTimestampIfFinite()?.let { EphemeralTimeout(it, null) }
        private set

    override fun noTimeout(): T = instance.also {
        timeout = null
    }

    override fun timeout(timeout: Duration): T = instance.also {
        val expirationTimestamp = timeout.toTimestampIfFinite() ?: throwUser("Timeout must be finite and positive")
        this.timeout = EphemeralTimeout(expirationTimestamp, null)
    }

    @JvmSynthetic
    override fun timeout(timeout: Duration, handler: suspend () -> Unit): T = instance.also {
        val expirationTimestamp = timeout.toTimestampIfFinite() ?: throwUser("Timeout must be finite and positive")
        this.timeout = EphemeralTimeout(expirationTimestamp, handler)
    }
}