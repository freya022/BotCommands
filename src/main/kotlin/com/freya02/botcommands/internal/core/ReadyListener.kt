package com.freya02.botcommands.internal.core

import com.freya02.botcommands.api.core.EventDispatcher
import com.freya02.botcommands.api.core.annotations.BEventListener
import com.freya02.botcommands.api.core.events.FirstGuildReadyEvent
import com.freya02.botcommands.api.core.events.InjectedJDAEvent
import com.freya02.botcommands.api.core.service.ServiceContainer
import com.freya02.botcommands.api.core.service.annotations.BService
import com.freya02.botcommands.api.core.service.putServiceAs
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.StatusChangeEvent
import net.dv8tion.jda.api.events.guild.GuildReadyEvent
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

@BService(name = "firstReadyListener")
internal class ReadyListener {
    private val lock = ReentrantLock()
    private var connected = false
    private var ready = false

    @BEventListener(priority = Int.MAX_VALUE)
    internal suspend fun onConnectEvent(
        event: StatusChangeEvent,
        eventDispatcher: EventDispatcher,
        serviceContainer: ServiceContainer
    ) {
        // At this point, JDA should be usable
        if (!connected && event.newStatus == JDA.Status.CONNECTING_TO_WEBSOCKET) {
            lock.withLock {
                if (connected) return
                connected = true
            }

            serviceContainer.putServiceAs(event.jda)

            eventDispatcher.dispatchEvent(InjectedJDAEvent(event.jda))
        }
    }

    @BEventListener(priority = Int.MAX_VALUE)
    internal suspend fun onGuildReadyEvent(
        event: GuildReadyEvent,
        eventDispatcher: EventDispatcher
    ) {
        if (ready) return
        lock.withLock {
            if (ready) return
            ready = true
        }

        eventDispatcher.dispatchEvent(FirstGuildReadyEvent(event))
    }
}