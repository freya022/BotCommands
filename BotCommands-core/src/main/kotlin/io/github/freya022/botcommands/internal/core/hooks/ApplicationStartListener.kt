package io.github.freya022.botcommands.internal.core.hooks

import io.github.freya022.botcommands.internal.core.events.BApplicationStartEvent

interface ApplicationStartListener {

    fun onApplicationStart(event: BApplicationStartEvent)
}
