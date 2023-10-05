package io.github.freya022.botcommands.test_kt.services

import io.github.freya022.botcommands.api.core.JDAService
import io.github.freya022.botcommands.api.core.events.BReadyEvent
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.test.Config
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.hooks.IEventManager
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder
import net.dv8tion.jda.api.utils.cache.CacheFlag

@BService
class Bot(private val config: Config) : JDAService() {
    override val intents: Set<GatewayIntent> =
        defaultIntents + GatewayIntent.GUILD_MEMBERS + GatewayIntent.MESSAGE_CONTENT

    override fun createJDA(event: BReadyEvent, eventManager: IEventManager) {
        DefaultShardManagerBuilder.createLight(config.token, intents).apply {
            enableCache(CacheFlag.FORUM_TAGS)
            setActivityProvider { Activity.playing("coroutines go brrr #$it") }
            setEventManagerProvider { eventManager }
            setShardsTotal(2)
            setShards(0, 1)
        }.build()
    }
}