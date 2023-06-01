package com.freya02.botcommands.internal

import com.freya02.botcommands.api.core.EventDispatcher
import com.freya02.botcommands.api.core.annotations.BEventListener
import com.freya02.botcommands.api.core.events.FirstReadyEvent
import com.freya02.botcommands.api.core.service.ServiceContainer
import com.freya02.botcommands.api.core.service.annotations.BService
import com.freya02.botcommands.api.core.service.putServiceAs
import net.dv8tion.jda.api.events.guild.GuildReadyEvent

@BService
internal class ReadyListener {
    private var ready = false

    @BEventListener
    internal suspend fun onGuildReadyEvent(
        event: GuildReadyEvent,
        eventDispatcher: EventDispatcher,
        serviceContainer: ServiceContainer
    ) {
        synchronized(this) {
            if (ready) return
            ready = true
        }

        serviceContainer.putServiceAs(event.jda)

        eventDispatcher.dispatchEvent(FirstReadyEvent(event))
    }
}