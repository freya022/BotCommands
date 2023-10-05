package com.freya02.botcommands.api.core.events

import com.freya02.botcommands.api.core.BContext

/**
 * Indicates the framework status changed to [BContext.Status.PRE_LOAD].
 */
class PreLoadEvent internal constructor() : BEvent()