package com.freya02.botcommands.internal.new_components.builder

import com.freya02.botcommands.api.new_components.builder.IEphemeralTimeoutableComponent
import com.freya02.botcommands.internal.new_components.new.EphemeralTimeout
import kotlinx.datetime.Clock
import kotlin.time.Duration

internal class EphemeralTimeoutableComponentImpl : IEphemeralTimeoutableComponent {
    override var timeout: EphemeralTimeout? = null
        private set

    override fun timeout(timeout: Duration) {
        this.timeout = EphemeralTimeout(Clock.System.now() + timeout, null)
    }

    @JvmSynthetic
    override fun timeout(timeout: Duration, handler: suspend () -> Unit) {
        this.timeout = EphemeralTimeout(Clock.System.now() + timeout, handler)
    }
}