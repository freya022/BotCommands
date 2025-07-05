package io.github.freya022.botcommands.api.core.events

import io.github.freya022.botcommands.api.core.BContext

interface BGenericEvent {
    val context: BContext
}