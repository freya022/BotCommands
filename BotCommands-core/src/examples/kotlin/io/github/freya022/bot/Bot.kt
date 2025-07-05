package io.github.freya022.bot

import io.github.freya022.bot.config.Config
import io.github.freya022.botcommands.api.core.JDAService
import io.github.freya022.botcommands.api.core.events.BReadyEvent
import io.github.freya022.botcommands.api.core.light
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.utils.enumSetOf
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.hooks.IEventManager
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.cache.CacheFlag

/**
 * Service to start JDA at the appropriate time
 */
@BService
class Bot(private val config: Config) : JDAService() {
    override val intents: Set<GatewayIntent> = defaultIntents()

    override val cacheFlags: Set<CacheFlag> = enumSetOf()

    override fun createJDA(event: BReadyEvent, eventManager: IEventManager) {
        // Creates a single shard, see docs
        light(
            token = config.token,
            activity = Activity.customStatus("In Kotlin with \u2764\uFE0F"),
        )
    }
}