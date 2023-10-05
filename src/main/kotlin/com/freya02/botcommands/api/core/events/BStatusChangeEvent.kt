package com.freya02.botcommands.api.core.events

import com.freya02.botcommands.api.core.BContext.Status

class BStatusChangeEvent internal constructor(val oldStatus: Status, val newStatus: Status) : BEvent()