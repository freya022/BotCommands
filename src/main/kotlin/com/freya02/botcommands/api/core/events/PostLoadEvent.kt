package com.freya02.botcommands.api.core.events

import com.freya02.botcommands.api.core.BContext

/**
 * Indicates the framework status changed to [BContext.Status.POST_LOAD].
 */
class PostLoadEvent internal constructor() : BEvent()