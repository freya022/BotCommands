package io.github.freya022.botcommands.api.core.events

import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.BContext.Status

class BStatusChangeEvent internal constructor(context: BContext, val oldStatus: Status, val newStatus: Status) : BEvent(context)