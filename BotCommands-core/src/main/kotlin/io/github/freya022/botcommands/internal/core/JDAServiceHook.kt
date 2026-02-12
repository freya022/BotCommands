package io.github.freya022.botcommands.internal.core

import io.github.freya022.botcommands.api.core.events.BReadyEvent
import net.dv8tion.jda.api.hooks.IEventManager

interface JDAServiceHook {
    fun onReadyEvent(
        event: BReadyEvent,
        eventManager: IEventManager,
        originalCall: (BReadyEvent, IEventManager) -> Unit,
    )
}
