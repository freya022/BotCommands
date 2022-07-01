package com.freya02.botcommands.commands.internal

import com.freya02.botcommands.core.api.annotations.BEventListener
import com.freya02.botcommands.core.api.annotations.BService
import com.freya02.botcommands.core.internal.EventDispatcher
import com.freya02.botcommands.core.internal.events.FirstReadyEvent
import dev.minn.jda.ktx.events.CoroutineEventManager
import net.dv8tion.jda.api.events.guild.GuildReadyEvent

@BService
internal class ReadyListener {
    private var ready = false

    @BEventListener
    internal suspend fun onGuildReadyEvent(event: GuildReadyEvent, eventDispatcher: EventDispatcher, coroutineEventManager: CoroutineEventManager) {
        synchronized(this) {
            if (ready) return
            ready = true
        }

        eventDispatcher.dispatchEvent(FirstReadyEvent(event))
    }
}