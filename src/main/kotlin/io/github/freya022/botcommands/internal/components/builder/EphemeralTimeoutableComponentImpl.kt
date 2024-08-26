package io.github.freya022.botcommands.internal.components.builder

import io.github.freya022.botcommands.api.components.Components
import io.github.freya022.botcommands.api.components.builder.IEphemeralTimeoutableComponent
import io.github.freya022.botcommands.internal.components.data.timeout.EphemeralTimeout
import io.github.freya022.botcommands.internal.utils.takeIfFinite
import io.github.freya022.botcommands.internal.utils.throwArgument
import kotlin.time.Duration

internal class EphemeralTimeoutableComponentImpl<T : IEphemeralTimeoutableComponent<T>> internal constructor(
    override val instanceRetriever: InstanceRetriever<T>
) : BuilderInstanceHolderImpl<T>(),
    IEphemeralTimeoutableComponent<T> {
    override var timeoutDuration: Duration? = Components.defaultTimeout.takeIfFinite()
    override var timeout: EphemeralTimeout? = null
        private set

    override var resetTimeoutOnUse: Boolean = false

    override fun resetTimeoutOnUse(resetTimeoutOnUse: Boolean): T = instance.also {
        this.resetTimeoutOnUse = resetTimeoutOnUse
    }

    override fun noTimeout(): T = instance.also {
        this.timeoutDuration = null
        this.timeout = null
    }

    override fun timeout(timeout: Duration): T = instance.also {
        this.timeoutDuration = timeout.takeIfFinite() ?: throwArgument("Timeout must be finite and positive")
        this.timeout = null
    }

    @JvmSynthetic
    override fun timeout(timeout: Duration, handler: suspend () -> Unit): T = instance.also {
        this.timeoutDuration = timeout.takeIfFinite() ?: throwArgument("Timeout must be finite and positive")
        this.timeout = EphemeralTimeout(handler)
    }
}