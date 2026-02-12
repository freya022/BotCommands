package dev.freya02.botcommands.jda.keepalive.internal.hooks

import dev.freya02.botcommands.jda.keepalive.api.config.JDAKeepAliveConfig
import dev.freya02.botcommands.jda.keepalive.internal.JDABuilderSession
import io.github.freya022.botcommands.api.core.config.getConfigOrNull
import io.github.freya022.botcommands.api.core.events.BReadyEvent
import io.github.freya022.botcommands.internal.core.JDAServiceHook
import net.dv8tion.jda.api.hooks.IEventManager

internal class KeepAliveJDAServiceHook : JDAServiceHook {

    override fun onReadyEvent(
        event: BReadyEvent,
        eventManager: IEventManager,
        originalCall: (BReadyEvent, IEventManager) -> Unit,
    ) {
        val config = event.context.config.getConfigOrNull<JDAKeepAliveConfig>()
        if (config == null) {
            return originalCall(event, eventManager)
        } else {
            JDABuilderSession.withBuilderSession(config.cacheKey) {
                originalCall(event, eventManager)
            }
        }
    }
}
