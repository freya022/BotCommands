package io.github.freya022.botcommands.internal.core

import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.JDAService
import io.github.freya022.botcommands.api.core.annotations.BEventListener
import io.github.freya022.botcommands.api.core.events.InjectedJDAEvent
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.getServiceOrNull
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.referenceString
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger { }

@BService
internal object JDAServiceMismatchChecker {
    @BEventListener
    internal fun onJDA(event: InjectedJDAEvent, context: BContext) {
        context.getServiceOrNull<JDAService>()?.let { jdaService ->
            val jdaIntents = event.jda.gatewayIntents
            val jdaServiceIntents = jdaService.intents
            if (jdaIntents != jdaServiceIntents) {
                logger.warn {
                    """
                        The intents given in JDAService and JDA should be the same!
                        JDA intents: $jdaIntents
                        JDAService intents: $jdaServiceIntents
                        Hint: you should pass ${JDAService::intents.referenceString} to your builder
                    """.trimIndent()
                }
            }
        }
    }
}