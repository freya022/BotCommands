package io.github.freya022.botcommands.api.core

import dev.minn.jda.ktx.events.CoroutineEventManager
import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService

@InterfacedService(acceptMultiple = false)
fun interface ICoroutineEventManagerSupplier {
    fun get(): CoroutineEventManager
}