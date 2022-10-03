package com.freya02.botcommands.internal.core.events

import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.api.ReceiverConsumer
import com.freya02.botcommands.api.core.events.BEvent

class BExceptionEvent(val context: BContext, val throwable: Throwable, private val event: Any) : BEvent() {
    fun <T> withEvent(type: Class<T>, consumer: ReceiverConsumer<T>) {
        if (type.isInstance(event)) {
            consumer.applyTo(type.cast(event))
        }
    }
}