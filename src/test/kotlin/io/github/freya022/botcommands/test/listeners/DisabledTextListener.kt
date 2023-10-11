package io.github.freya022.botcommands.test.listeners

import io.github.freya022.botcommands.api.core.annotations.BEventListener
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

private val logger = KotlinLogging.logger { }

@BService
class DisabledTextListener {
    @BEventListener
    fun onMessage(event: MessageReceivedEvent) {
        logger.trace("Received message in ${this.javaClass.simpleNestedName}" )
    }
}