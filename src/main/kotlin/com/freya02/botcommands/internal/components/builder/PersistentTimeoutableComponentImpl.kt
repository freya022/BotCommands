package com.freya02.botcommands.internal.components.builder

import com.freya02.botcommands.api.components.builder.IPersistentTimeoutableComponent
import com.freya02.botcommands.internal.components.new.PersistentTimeout
import kotlinx.datetime.Clock
import kotlin.time.Duration

internal class PersistentTimeoutableComponentImpl : IPersistentTimeoutableComponent {
    override var timeout: PersistentTimeout? = null
        private set

    override fun timeout(timeout: Duration) {
        this.timeout = PersistentTimeout(Clock.System.now() + timeout, null, emptyArray())
    }

    override fun timeout(timeout: Duration, handlerName: String, vararg args: Any?) {
        this.timeout = PersistentTimeout(Clock.System.now() + timeout, handlerName, args)
    }
}