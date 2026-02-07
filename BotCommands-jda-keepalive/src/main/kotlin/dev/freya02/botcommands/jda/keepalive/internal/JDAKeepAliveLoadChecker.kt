package dev.freya02.botcommands.jda.keepalive.internal

import dev.freya02.botcommands.jda.keepalive.internal.annotations.RequiresJDAKeepAlive
import io.github.freya022.botcommands.api.core.annotations.BEventListener
import io.github.freya022.botcommands.api.core.events.PreLoadEvent
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger { }

@BService
@RequiresJDAKeepAlive
internal class JDAKeepAliveLoadChecker {

    @BEventListener
    fun onPreLoad(event: PreLoadEvent) {
        if (!Agent.isLoaded) {
            logger.info { "The JDA keepalive module is present but the agent has not been loaded, please check the instructions" }
        }
    }
}
