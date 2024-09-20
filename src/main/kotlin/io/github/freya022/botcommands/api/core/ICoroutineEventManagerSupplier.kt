package io.github.freya022.botcommands.api.core

import dev.minn.jda.ktx.events.CoroutineEventManager
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService

/**
 * Interface to supply a [CoroutineEventManager], ran once at startup.
 *
 * **Usage**: Register your instance as a service with [@BService][BService].
 *
 * @see InterfacedService @InterfacedService
 */
@InterfacedService(acceptMultiple = false)
fun interface ICoroutineEventManagerSupplier {
    fun get(): CoroutineEventManager
}