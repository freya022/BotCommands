package io.github.freya022.botcommands.api.core.events

import io.github.freya022.botcommands.api.core.BContext

/**
 * Indicates the framework status changed to [BContext.Status.LOAD].
 */
class LoadEvent internal constructor() : BEvent()