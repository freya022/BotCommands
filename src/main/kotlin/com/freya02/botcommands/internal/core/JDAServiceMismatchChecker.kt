package com.freya02.botcommands.internal.core

import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.api.core.JDAService
import com.freya02.botcommands.api.core.annotations.BEventListener
import com.freya02.botcommands.api.core.events.InjectedJDAEvent
import com.freya02.botcommands.api.core.service.annotations.BService
import com.freya02.botcommands.api.core.service.getServiceOrNull
import com.freya02.botcommands.internal.utils.ReflectionUtils.referenceString
import mu.KotlinLogging

private val logger = KotlinLogging.logger { }

@BService
internal object JDAServiceMismatchChecker {
    @BEventListener
    internal fun onJDA(event: InjectedJDAEvent, context: BContext) {
        context.getServiceOrNull<JDAService>()?.let { jdaService ->
            val jdaIntents = event.jda.gatewayIntents
            val jdaServiceIntents = jdaService.intents
            if (jdaIntents != jdaServiceIntents) {
                logger.warn(
                    """
                        The intents given in JDAService and JDA should be the same!
                        JDA intents: $jdaIntents
                        JDAService intents: $jdaServiceIntents
                        Hint: you should pass ${JDAService::intents.referenceString} to your builder
                    """.trimIndent()
                )
            }
        }
    }
}