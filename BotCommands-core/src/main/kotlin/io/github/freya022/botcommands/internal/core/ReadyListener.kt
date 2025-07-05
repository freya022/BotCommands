package io.github.freya022.botcommands.internal.core

import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.annotations.BEventListener
import io.github.freya022.botcommands.api.core.annotations.BEventListener.RunMode
import io.github.freya022.botcommands.api.core.events.FirstGuildReadyEvent
import io.github.freya022.botcommands.api.core.events.InjectedJDAEvent
import io.github.freya022.botcommands.api.core.events.PreFirstGatewayConnectEvent
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.putServiceAs
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.StatusChangeEvent
import net.dv8tion.jda.api.events.guild.GuildReadyEvent
import net.dv8tion.jda.api.sharding.ShardManager
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

@BService(name = "bcFirstReadyListener")
internal class ReadyListener {
    private val lock = ReentrantLock()
    private var connected = false
    private var ready = false

    // Blocking so [[PreFirstGatewayConnectEvent]] can block the JDA thread and do their things
    @BEventListener(priority = Int.MAX_VALUE, mode = RunMode.BLOCKING)
    internal suspend fun onConnectEvent(event: StatusChangeEvent, context: BContext) {
        // At this point, JDA should be usable
        if (!connected && event.newStatus == JDA.Status.CONNECTING_TO_WEBSOCKET) {
            lock.withLock {
                if (connected) return
                connected = true
            }

            context.serviceContainer.putServiceAs<JDA>(event.jda)
            event.jda.shardManager?.let { context.serviceContainer.putServiceAs<ShardManager>(it) }

            context.eventDispatcher.dispatchEvent(InjectedJDAEvent(context, event.jda))
            // Added so it makes a bit more sense for the user
            context.eventDispatcher.dispatchEvent(PreFirstGatewayConnectEvent(context, event.jda))
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