package com.freya02.botcommands.api.core.events

import com.freya02.botcommands.api.core.BContext

/**
 * Indicates the framework status changed to [BContext.Status.LOAD].
 */
class LoadEvent internal constructor() : BEvent()