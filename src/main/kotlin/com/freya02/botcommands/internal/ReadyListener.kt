package com.freya02.botcommands.internal

import com.freya02.botcommands.api.core.EventDispatcher
import com.freya02.botcommands.api.core.annotations.BEventListener
import com.freya02.botcommands.api.core.events.FirstReadyEvent
import com.freya02.botcommands.api.core.service.ServiceContainer
import com.freya02.botcommands.api.core.service.annotations.BService
import com.freya02.botcommands.api.core.service.putServiceAs
import net.dv8tion.jda.api.events.guild.GuildReadyEvent
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

@BService(name = "firstReadyListener")
internal class ReadyListener {
    private val lock = ReentrantLock()
    private var ready = false

    @BEventListener(priority = Int.MAX_VALUE)
    internal suspend fun onGuildReadyEvent(
        event: GuildReadyEvent,
        eventDispatcher: EventDispatcher,
        serviceContainer: ServiceContainer
    ) {
        if (ready) return
        lock.withLock {
            if (ready) return
            ready = true
        }

        serviceContainer.putServiceAs(event.jda)

        eventDispatcher.dispatchEvent(FirstReadyEvent(event))
    }
}