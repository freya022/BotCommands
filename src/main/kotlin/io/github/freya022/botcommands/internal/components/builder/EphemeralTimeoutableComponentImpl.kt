package io.github.freya022.botcommands.internal.components.builder

import io.github.freya022.botcommands.api.components.Components
import io.github.freya022.botcommands.api.components.builder.IEphemeralTimeoutableComponent
import io.github.freya022.botcommands.internal.components.data.EphemeralTimeout
import io.github.freya022.botcommands.internal.utils.throwArgument
import io.github.freya022.botcommands.internal.utils.toTimestampIfFinite
import kotlinx.datetime.Instant
import kotlin.time.Duration

internal class EphemeralTimeoutableComponentImpl<T : IEphemeralTimeoutableComponent<T>> internal constructor(
    override val instanceRetriever: InstanceRetriever<T>
) : BuilderInstanceHolderImpl<T>(),
    IEphemeralTimeoutableComponent<T> {
    override var expiresAt: Instant? = Components.defaultTimeout.toTimestampIfFinite()
    override var timeout: EphemeralTimeout? = null
        private set

    override fun noTimeout(): T = instance.also {
        this.expiresAt = null
        this.timeout = null
    }

    override fun timeout(timeout: Duration): T = instance.also {
        this.expiresAt = timeout.toTimestampIfFinite() ?: throwArgument("Timeout must be finite and positive")
        this.timeout = null
    }

    @JvmSynthetic
    override fun timeout(timeout: Duration, handler: suspend () -> Unit): T = instance.also {
        this.expiresAt = timeout.toTimestampIfFinite() ?: throwArgument("Timeout must be finite and positive")
        this.timeout = EphemeralTimeout(handler)
    }
}