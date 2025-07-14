package io.github.freya022.botcommands.api.core.hooks

import net.dv8tion.jda.api.events.GenericEvent

interface CoroutineEventListener {

    suspend fun onEvent(event: GenericEvent)
}
