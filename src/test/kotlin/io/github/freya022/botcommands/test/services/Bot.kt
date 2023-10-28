package io.github.freya022.botcommands.test.services

import io.github.freya022.botcommands.api.core.JDAService
import io.github.freya022.botcommands.api.core.events.BReadyEvent
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.utils.enumSetOf
import io.github.freya022.botcommands.test.config.Config
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.hooks.IEventManager
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder
import net.dv8tion.jda.api.utils.MemberCachePolicy
import net.dv8tion.jda.api.utils.cache.CacheFlag

@BService
class Bot(private val config: Config) : JDAService() {
    override val intents: Set<GatewayIntent> =
        defaultIntents + GatewayIntent.GUILD_MEMBERS + GatewayIntent.MESSAGE_CONTENT

    override val cacheFlags: Set<CacheFlag> =
        enumSetOf(CacheFlag.FORUM_TAGS, CacheFlag.VOICE_STATE)

    override fun createJDA(event: BReadyEvent, eventManager: IEventManager) {
        DefaultShardManagerBuilder.createLight(config.token, intents).apply {
            enableCache(cacheFlags)
            setMemberCachePolicy(MemberCachePolicy.VOICE)
            setActivityProvider { Activity.playing("coroutines go brrr #$it") }
            setEventManagerProvider { eventManager }
            setShardsTotal(2)
            setShards(0, 1)
        }.build()
    }
}