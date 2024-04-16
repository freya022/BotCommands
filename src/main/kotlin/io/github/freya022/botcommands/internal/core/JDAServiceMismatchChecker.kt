package io.github.freya022.botcommands.internal.core

import io.github.freya022.botcommands.api.core.JDAService
import io.github.freya022.botcommands.api.core.annotations.BEventListener
import io.github.freya022.botcommands.api.core.events.InjectedJDAEvent
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.utils.enumSetOf
import io.github.freya022.botcommands.internal.utils.reference
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.cache.CacheFlag
import org.springframework.core.env.Environment
import org.springframework.core.env.getProperty
import org.springframework.stereotype.Component

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

// Additional checks to check the properties are the same as in JDAService
@Component
internal class SpringJDAServiceMismatchChecker {
    @BEventListener
    internal fun onJDA(event: InjectedJDAEvent, environment: Environment, jdaService: JDAService) {
        val environmentIntents = environment
            .getProperty<List<String>>("jda.intents")
            ?.mapTo(enumSetOf<GatewayIntent>(), GatewayIntent::valueOf)
            ?: JDAService.defaultIntents
        val jdaServiceIntents = jdaService.intents
        if (environmentIntents != jdaServiceIntents) {
            logger.warn {
                """
                    The intents given in JDAService and the environment should be the same!
                    Environment intents: $environmentIntents
                    JDAService intents: $jdaServiceIntents
                    Hint: you should pass the "jda.intents" value to your builder
                """.trimIndent()
            }
        }

        val environmentCacheFlags = environment
            .getProperty<List<String>>("jda.cacheFlags")
            ?.mapTo(enumSetOf(), CacheFlag::valueOf)
            ?: enumSetOf()
        val jdaServiceCacheFlags = jdaService.cacheFlags
        if (environmentCacheFlags != jdaServiceCacheFlags) {
            logger.warn {
                """
                    The cache flags given in JDAService and the environment should be the same!
                    Environment cache flags: $environmentCacheFlags
                    JDAService cache flags: $jdaServiceCacheFlags
                    Hint: you should pass the "jda.cacheFlags" value to your builder
                """.trimIndent()
            }
        }
    }
}