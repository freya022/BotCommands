package io.github.freya022.botcommands.api.core.hooks

import io.github.freya022.botcommands.api.core.annotations.BEventListener
import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.runBlocking

/**
 * Dispatches JDA and BC events to [@BEventListener][BEventListener] methods.
 */
@InterfacedService(acceptMultiple = false)
abstract class EventDispatcher internal constructor() {

    abstract fun addEventListener(listener: Any)

    abstract fun removeEventListener(listener: Any)

    @JvmSynthetic
    abstract suspend fun dispatchEvent(event: Any)

    @JvmName("dispatchEvent")
    fun dispatchEventJava(event: Any) = runBlocking { dispatchEvent(event) }

    abstract fun dispatchEventAsync(event: Any): List<Deferred<Unit>>
}