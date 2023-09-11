package com.freya02.botcommands.test_kt.services

import com.freya02.botcommands.api.core.JDAService
import com.freya02.botcommands.api.core.events.BReadyEvent
import com.freya02.botcommands.api.core.service.annotations.BService
import com.freya02.botcommands.test.Config
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.hooks.IEventManager
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder
import net.dv8tion.jda.api.utils.cache.CacheFlag

@BService
class Bot(private val config: Config) : JDAService() {
    override val intents: Set<GatewayIntent> =
        GatewayIntent.getIntents(GatewayIntent.DEFAULT) + GatewayIntent.GUILD_MEMBERS + GatewayIntent.MESSAGE_CONTENT

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