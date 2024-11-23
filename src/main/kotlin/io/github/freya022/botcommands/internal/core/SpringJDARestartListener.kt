package io.github.freya022.botcommands.internal.core

import io.github.freya022.botcommands.api.core.config.JDAConfiguration
import io.github.freya022.botcommands.api.core.utils.awaitShutdown
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.JDA
import org.springframework.beans.factory.getBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.event.ContextClosedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger { }

@Component
@ConditionalOnProperty(value = ["spring.devtools.restart.enabled", "jda.devtools.enabled"], havingValue = "true", matchIfMissing = true)
internal class SpringJDARestartListener(
    private val jdaConfiguration: JDAConfiguration,
) {

    @EventListener
    internal fun onContextClosed(event: ContextClosedEvent) {
        logger.info { "Shutting down JDA" }

        val jda = event.applicationContext.getBean<JDA>()
        jda.shutdown()

        if (!jda.awaitShutdown(jdaConfiguration.devTools.shutdownTimeout)) {
            logger.warn { "Timed out waiting for JDA to shutdown, forcing" }

            jda.shutdownNow()
            jda.awaitShutdown()
        } else {
            logger.info { "JDA has gracefully shut down" }
        }
    }
}