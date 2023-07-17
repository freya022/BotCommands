package com.freya02.botcommands.test_kt.services

import com.freya02.botcommands.api.core.service.ServiceStart
import com.freya02.botcommands.api.core.service.annotations.BService
import com.freya02.botcommands.test.Config
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.hooks.IEventManager
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder
import net.dv8tion.jda.api.utils.cache.CacheFlag

@BService(ServiceStart.READY)
class JDAService(config: Config, eventManager: IEventManager) {
    init {
        DefaultShardManagerBuilder.createLight(config.token).apply {
            enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.MESSAGE_CONTENT)
            enableCache(CacheFlag.FORUM_TAGS)
            setActivityProvider { Activity.playing("coroutines go brrr #$it") }
            setEventManagerProvider { eventManager }
            setShardsTotal(2)
            setShards(0, 1)
        }.build()
    }
}