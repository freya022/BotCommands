package io.github.freya022.botcommands.internal.core

import io.github.freya022.botcommands.api.core.JDAService
import io.github.freya022.botcommands.api.core.annotations.BEventListener
import io.github.freya022.botcommands.api.core.events.InjectedJDAEvent
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.internal.utils.reference
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger { }

@BService
internal object JDAServiceMismatchChecker {
    @BEventListener
    internal fun onJDA(event: InjectedJDAEvent, jdaService: JDAService) {
        val jdaIntents = event.jda.gatewayIntents
        val jdaServiceIntents = jdaService.intents
        if (jdaIntents != jdaServiceIntents) {
            logger.warn {
                """
                    The intents given in JDAService and JDA should be the same!
                    JDA intents: $jdaIntents
                    JDAService intents: $jdaServiceIntents
                    Hint: you should pass ${JDAService::intents.reference} to your builder
                """.trimIndent()
            }
        }

        val jdaCacheFlags = event.jda.cacheFlags
        val jdaServiceCacheFlags = jdaService.cacheFlags
        if (!jdaCacheFlags.containsAll(jdaServiceCacheFlags)) {
            logger.warn {
                """
                    The cache flags given in JDAService should at least be a subset of the JDA cache flags!
                    JDA cache flags: $jdaCacheFlags
                    JDAService cache flags: $jdaServiceCacheFlags
                    Hint: you should pass ${JDAService::cacheFlags.reference} to your builder
                """.trimIndent()
            }
        }
    }
}