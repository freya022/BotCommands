package io.github.freya022.botcommands.internal.components.builder

import io.github.freya022.botcommands.api.components.Components
import io.github.freya022.botcommands.api.components.builder.IEphemeralTimeoutableComponent
import io.github.freya022.botcommands.internal.components.data.EphemeralTimeout
import kotlinx.datetime.Clock
import kotlin.time.Duration

internal class EphemeralTimeoutableComponentImpl<T : IEphemeralTimeoutableComponent<T>> internal constructor(
    override val instanceRetriever: InstanceRetriever<T>
) : BuilderInstanceHolderImpl<T>(),
    IEphemeralTimeoutableComponent<T> {
    override var timeout: EphemeralTimeout = EphemeralTimeout(Clock.System.now() + Components.defaultTimeout, null)
        private set

    override fun timeout(timeout: Duration): T = instance.also {
        this.timeout = EphemeralTimeout(Clock.System.now() + timeout, null)
    }

    @JvmSynthetic
    override fun timeout(timeout: Duration, handler: suspend () -> Unit): T = instance.also {
        this.timeout = EphemeralTimeout(Clock.System.now() + timeout, handler)
    }
}