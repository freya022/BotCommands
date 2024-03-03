package io.github.freya022.botcommands.internal.core

import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.annotations.BEventListener
import io.github.freya022.botcommands.api.core.events.FirstGuildReadyEvent
import io.github.freya022.botcommands.api.core.events.InjectedJDAEvent
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.putServiceAs
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.StatusChangeEvent
import net.dv8tion.jda.api.events.guild.GuildReadyEvent
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

@BService(name = "bcFirstReadyListener")
internal class ReadyListener {
    private val lock = ReentrantLock()
    private var connected = false
    private var ready = false

    @BEventListener(priority = Int.MAX_VALUE)
    internal suspend fun onConnectEvent(event: StatusChangeEvent, context: BContext) {
        // At this point, JDA should be usable
        if (!connected && event.newStatus == JDA.Status.CONNECTING_TO_WEBSOCKET) {
            lock.withLock {
                if (connected) return
                connected = true
            }

            context.putServiceAs(event.jda)
            event.jda.shardManager?.let { context.putServiceAs(it) }

            context.eventDispatcher.dispatchEvent(InjectedJDAEvent(context, event.jda))
        }
    }

    @BEventListener(priority = Int.MAX_VALUE)
    internal suspend fun onGuildReadyEvent(event: GuildReadyEvent, context: BContext) {
        if (ready) return
        lock.withLock {
            if (ready) return
            ready = true
        }

        context.eventDispatcher.dispatchEvent(FirstGuildReadyEvent(context, event))
    }
}