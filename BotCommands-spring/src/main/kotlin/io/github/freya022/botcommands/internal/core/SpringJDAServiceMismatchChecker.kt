package io.github.freya022.botcommands.internal.core

import io.github.freya022.botcommands.api.core.JDAService
import io.github.freya022.botcommands.api.core.annotations.BEventListener
import io.github.freya022.botcommands.api.core.config.JDAConfiguration
import io.github.freya022.botcommands.api.core.events.InjectedJDAEvent
import io.github.freya022.botcommands.internal.utils.reference
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger { }

// Spring checks are slightly different, we want to tell the user to move them to their application environment,
// so the checks are consistent with condition annotations, as they can only check the environment
@Component
internal class SpringJDAServiceMismatchChecker {
    @BEventListener
    internal fun onJDA(event: InjectedJDAEvent, jdaConfiguration: JDAConfiguration, jdaService: JDAService) {
        val environmentIntents = jdaConfiguration.intents
        val jdaServiceIntents = jdaService.intents
        if (environmentIntents != jdaServiceIntents) {
            logger.warn {
                """
                    The intents given in JDAService and the environment should be the same!
                    Environment intents: ${environmentIntents.sorted()}
                    JDAService intents: ${jdaServiceIntents.sorted()}
                    Hint: you should get your intents from ${JDAConfiguration::intents.reference}
                """.trimIndent()
            }
        }

        val environmentCacheFlags = jdaConfiguration.cacheFlags
        val jdaServiceCacheFlags = jdaService.cacheFlags
        if (environmentCacheFlags != jdaServiceCacheFlags) {
            logger.warn {
                """
                    The cache flags given in JDAService and the environment should be the same!
                    Environment cache flags: ${environmentCacheFlags.sorted()}
                    JDAService cache flags: ${jdaServiceCacheFlags.sorted()}
                    Hint: you should get your caches flags from ${JDAConfiguration::cacheFlags.reference}
                """.trimIndent()
            }
        }
    }
}