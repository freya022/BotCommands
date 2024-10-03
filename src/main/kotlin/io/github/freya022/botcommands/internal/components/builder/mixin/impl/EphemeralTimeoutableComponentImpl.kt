package io.github.freya022.botcommands.internal.components.builder.mixin.impl

import io.github.freya022.botcommands.api.components.Components
import io.github.freya022.botcommands.api.components.builder.IEphemeralTimeoutableComponent
import io.github.freya022.botcommands.internal.components.builder.BuilderInstanceHolderImpl
import io.github.freya022.botcommands.internal.components.builder.InstanceRetriever
import io.github.freya022.botcommands.internal.components.builder.mixin.IEphemeralTimeoutableComponentMixin
import io.github.freya022.botcommands.internal.components.data.timeout.EphemeralTimeout
import io.github.freya022.botcommands.internal.utils.Checks
import io.github.freya022.botcommands.internal.utils.takeIfFinite
import kotlin.time.Duration

internal class EphemeralTimeoutableComponentImpl<T : IEphemeralTimeoutableComponent<T>> internal constructor(
    override val instanceRetriever: InstanceRetriever<T>
) : BuilderInstanceHolderImpl<T>(),
    IEphemeralTimeoutableComponentMixin<T> {

    override var timeoutDuration: Duration? = Components.defaultEphemeralTimeout?.takeIfFinite()
    override var timeout: EphemeralTimeout? = null

    override var resetTimeoutOnUse: Boolean = false

    override fun resetTimeoutOnUse(resetTimeoutOnUse: Boolean): T = applyInstance {
        this.resetTimeoutOnUse = resetTimeoutOnUse
    }

    override fun noTimeout(): T = applyInstance {
        this.timeoutDuration = null
        this.timeout = null
    }

    override fun timeout(timeout: Duration): T = applyInstance {
        Checks.checkFinite(timeout, "timeout")
        Checks.checkFitInt(timeout, "timeout")

        this.timeoutDuration = timeout
        this.timeout = null
    }

    @JvmSynthetic
    override fun timeout(timeout: Duration, handler: suspend () -> Unit): T = applyInstance {
        Checks.checkFinite(timeout, "timeout")
        Checks.checkFitInt(timeout, "timeout")

        this.timeoutDuration = timeout
        this.timeout = EphemeralTimeout(handler)
    }
}