package com.freya02.botcommands.internal.new_components.builder

import com.freya02.botcommands.api.new_components.builder.IPersistentTimeoutableComponent
import com.freya02.botcommands.internal.new_components.new.PersistentTimeout
import kotlinx.datetime.Clock
import kotlin.time.Duration

class PersistentTimeoutableComponentImpl<T : IPersistentTimeoutableComponent<T>> : IPersistentTimeoutableComponent<T> {
    override var timeout: PersistentTimeout? = null
        private set

    override fun timeout(timeout: Duration, handlerName: String, vararg args: Any?): T = this.also {
        this.timeout = PersistentTimeout(Clock.System.now() + timeout, handlerName, args)
    } as T
}