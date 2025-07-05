package io.github.freya022.botcommands.framework

import io.github.freya022.botcommands.api.core.JDAService
import io.github.freya022.botcommands.api.core.events.BReadyEvent
import io.github.freya022.botcommands.api.core.service.annotations.BService
import net.dv8tion.jda.api.hooks.IEventManager
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.cache.CacheFlag

@BService
class FakeBot : JDAService() {
    override val intents: Set<GatewayIntent>
        get() = emptySet()
    override val cacheFlags: Set<CacheFlag>
        get() = emptySet()

    override fun createJDA(event: BReadyEvent, eventManager: IEventManager) {}
}