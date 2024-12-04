package io.github.freya022.botcommands.internal.core.hooks

import dev.minn.jda.ktx.events.CoroutineEventManager
import net.dv8tion.jda.api.events.GenericEvent

internal class DispatcherAwareCoroutineEventManager internal constructor(
    originalCoroutineEventManager: CoroutineEventManager,
    private val eventDispatcher: EventDispatcherImpl,
) : CoroutineEventManager(originalCoroutineEventManager, originalCoroutineEventManager.timeout) {

    override fun handle(event: GenericEvent) {
        eventDispatcher.onEvent(event)
        super.handle(event) // Still let users use their own registered event listeners
    }
}