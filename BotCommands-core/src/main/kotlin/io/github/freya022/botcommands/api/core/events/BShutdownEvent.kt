package io.github.freya022.botcommands.api.core.events

import io.github.freya022.botcommands.api.core.BContext

/**
 * Event fired after the framework status changed to [SHUTDOWN][BContext.Status.SHUTDOWN].
 */
class BShutdownEvent internal constructor(context: BContext) : BEvent(context)
