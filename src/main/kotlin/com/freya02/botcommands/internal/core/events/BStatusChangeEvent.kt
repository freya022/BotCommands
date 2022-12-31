package com.freya02.botcommands.internal.core.events

import com.freya02.botcommands.api.BContext.Status
import com.freya02.botcommands.api.core.events.BEvent

class BStatusChangeEvent internal constructor(val oldStatus: Status, val newStatus: Status) : BEvent()