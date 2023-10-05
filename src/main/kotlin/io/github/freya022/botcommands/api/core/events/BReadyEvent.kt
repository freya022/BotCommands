package io.github.freya022.botcommands.api.core.events

import io.github.freya022.botcommands.api.core.BContext

/**
 * Indicates the framework status changed to [BContext.Status.READY].
 */
class BReadyEvent internal constructor() : BEvent()