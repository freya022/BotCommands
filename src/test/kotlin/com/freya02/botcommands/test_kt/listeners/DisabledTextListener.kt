package com.freya02.botcommands.test_kt.listeners

import com.freya02.botcommands.api.core.annotations.BEventListener
import com.freya02.botcommands.api.core.service.annotations.BService
import com.freya02.botcommands.api.core.utils.simpleNestedName
import mu.KotlinLogging
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

private val logger = KotlinLogging.logger { }

@BService
class DisabledTextListener {
    @BEventListener
    fun onMessage(event: MessageReceivedEvent) {
        logger.trace("Received message in ${this.javaClass.simpleNestedName}" )
    }
}