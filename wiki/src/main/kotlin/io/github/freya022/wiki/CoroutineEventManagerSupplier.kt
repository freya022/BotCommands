package io.github.freya022.wiki

import dev.minn.jda.ktx.events.CoroutineEventManager
import io.github.freya022.botcommands.api.core.ICoroutineEventManagerSupplier
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.utils.namedDefaultScope

// --8<-- [start:coroutine_event_manager_supplier-kotlin]
@BService
class CoroutineEventManagerSupplier : ICoroutineEventManagerSupplier {
    override fun get(): CoroutineEventManager {
        val scope = namedDefaultScope("WikiBot Coroutine", corePoolSize = 4)
        return CoroutineEventManager(scope)
    }
}
// --8<-- [end:coroutine_event_manager_supplier-kotlin]