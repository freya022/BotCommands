package io.github.freya022.botcommands.api.core

import io.github.freya022.botcommands.api.core.annotations.BEventListener
import io.github.freya022.botcommands.api.core.conditions.RequiredIntents
import io.github.freya022.botcommands.api.core.config.BServiceConfigBuilder
import io.github.freya022.botcommands.api.core.events.BReadyEvent
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.hooks.IEventManager
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder
import net.dv8tion.jda.api.sharding.ShardManager
import net.dv8tion.jda.api.utils.cache.CacheFlag
import java.util.*

/**
 * Optional interfaced service to be implemented by the service which creates a JDA instance.
 *
 * **Usage**: Register your instance as a service with [BService]
 * or [any annotation that enables your class for dependency injection][BServiceConfigBuilder.serviceAnnotations].
 *
 * Example:
 * ```kt
 * @BService
 * class Bot(private val config: Config) : JDAService() {
 *     override val intents: Set<GatewayIntent> = enumSetOf(GatewayIntent.MESSAGE_CONTENT)
 *
 *     override val cacheFlags: Set<CacheFlag> = enumSetOf()
 *
 *     override fun createJDA(event: BReadyEvent, eventManager: IEventManager) {
 *         DefaultShardManagerBuilder.createLight(config.token, intents).apply {
 *             setEventManagerProvider { eventManager }
 *             enableCache(cacheFlags)
 *             ...
 *         }.build()
 *     }
 * }
 * ```
 *
 * @see createJDA
 * @see InterfacedService @InterfacedService
 * @see RequiredIntents @RequiredIntents
 */
@InterfacedService(acceptMultiple = false)
abstract class JDAService {
    /**
     * The intents used by your bot,
     * must be passed as the entire list of intents your bot will use,
     * i.e., JDABuilder's create(Light/Default) methods and similar for shard managers.
     *
     * @see defaultIntents
     */
    abstract val intents: Set<GatewayIntent>

    /**
     * The cache flags used by your bot,
     * must at least be a subset of the cache flags your bot will use.
     *
     * To make sure JDA uses these flags,
     * you can pass these to [JDABuilder.enableCache] / [DefaultShardManagerBuilder.enableCache].
     */
    abstract val cacheFlags: Set<CacheFlag>

    /**
     * Creates a [JDA] or [ShardManager] instance.
     *
     * The framework will pick up the JDA instance (or one of its shards) automatically,
     * but for that you **need** to use the provided [eventManager] in either:
     * - [jda.setEventManager(eventManager)][JDA.setEventManager]
     * - [shardManagerBuilder.setEventManagerProvider { eventManager }][DefaultShardManagerBuilder.setEventManagerProvider]
     *
     * @param event        The framework's ready event
     * @param eventManager The event manager passed to [BBuilder.newBuilder], you **must** use it in your [JDABuilder]/[DefaultShardManagerBuilder]
     *
     */
    abstract fun createJDA(event: BReadyEvent, eventManager: IEventManager)

    @JvmSynthetic
    @BEventListener
    internal fun onReadyEvent(event: BReadyEvent, eventManager: IEventManager) = createJDA(event, eventManager)

    companion object {
        /**
         * Returns the default JDA intents.
         *
         * @see GatewayIntent.DEFAULT
         */
        @JvmStatic
        val defaultIntents: EnumSet<GatewayIntent>
            get() = GatewayIntent.getIntents(GatewayIntent.DEFAULT)
    }
}