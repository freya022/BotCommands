package io.github.freya022.botcommands.api.core

import dev.minn.jda.ktx.events.CoroutineEventManager
import io.github.freya022.botcommands.api.core.JDAService.Companion.getDefaultRestConfig
import io.github.freya022.botcommands.api.core.annotations.BEventListener
import io.github.freya022.botcommands.api.core.conditions.RequiredIntents
import io.github.freya022.botcommands.api.core.config.JDAConfiguration
import io.github.freya022.botcommands.api.core.events.BReadyEvent
import io.github.freya022.botcommands.api.core.events.InjectedJDAEvent
import io.github.freya022.botcommands.api.core.requests.PriorityGlobalRestRateLimiter
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService
import io.github.freya022.botcommands.api.core.service.annotations.MissingServiceMessage
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.hooks.IEventManager
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.requests.RestConfig
import net.dv8tion.jda.api.requests.RestRateLimiter
import net.dv8tion.jda.api.requests.RestRateLimiter.RateLimitConfig
import net.dv8tion.jda.api.requests.SequentialRestRateLimiter
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder
import net.dv8tion.jda.api.sharding.ShardManager
import net.dv8tion.jda.api.utils.ChunkingFilter
import net.dv8tion.jda.api.utils.MemberCachePolicy
import net.dv8tion.jda.api.utils.cache.CacheFlag
import java.util.*
import javax.annotation.CheckReturnValue

/**
 * Interfaced service to be implemented by the service which creates a JDA instance.
 *
 * This has many advantages:
 * - Checking gateway intents, cache flags, and member cache requirements for event listeners and event waiters
 * - Conditionally enabling services based on gateway intents ([@RequiredIntents][RequiredIntents]),
 * cache flags, and member cache
 * - Starting JDA when every other service is ready
 *
 * ### Usage
 * Register your instance as a service with [@BService][BService].
 *
 * Example:
 * ```kt
 * @BService
 * class Bot(private val config: Config) : JDAService() {
 *     // Default intents + MESSAGE_CONTENT
 *     override val intents: Set<GatewayIntent> = defaultIntents(GatewayIntent.MESSAGE_CONTENT)
 *
 *     override val cacheFlags: Set<CacheFlag> = enumSetOf()
 *
 *     override fun createJDA(event: BReadyEvent, eventManager: IEventManager) {
 *         // Read the docs on what this does
 *         lightSharded(config.token, ...) {
 *             ...
 *         }
 *     }
 * }
 * ```
 *
 * #### Spring support
 * Spring users must set their gateway intents and cache flags using properties,
 * named `jda.intents` and `jda.cacheFlags` respectively, also available in [JDAConfiguration].
 *
 * @see createJDA
 * @see InterfacedService @InterfacedService
 * @see RequiredIntents @RequiredIntents
 */
@InterfacedService(acceptMultiple = false)
@MissingServiceMessage("A service extending JDAService must exist and has to be in the search path")
abstract class JDAService {
    // * A private backing property is used to hide details from Java users
    private lateinit var _eventManager: IEventManager
    @PublishedApi
    @get:JvmSynthetic
    internal val eventManager get() = _eventManager

    /**
     * The intents used by your bot,
     * must be passed as the entire list of intents your bot will use,
     * i.e., JDABuilder's `create(Light/Default)` methods and similar for shard managers,
     * do not use [JDABuilder.enableIntents].
     *
     * @see defaultIntents
     */
    abstract val intents: Set<GatewayIntent>

    /**
     * The cache flags used by your bot,
     * the provided cache flags must all be present in [JDA.getCacheFlags].
     *
     * To make sure JDA uses these flags,
     * you can pass these to [JDABuilder.enableCache] / [DefaultShardManagerBuilder.enableCache].
     */
    abstract val cacheFlags: Set<CacheFlag>

    /**
     * Creates a [JDA] or [ShardManager] instance.
     *
     * I recommend using any of [create]/[default]/[light],
     * this sets up the correct intents, cache flags, rest config and event manager.
     *
     * You can alternatively use JDA directly, but you'll need to pass the [eventManager] to it.
     *
     * After a shard is started, a JDA instance will be picked up automatically (assuming you set the event manager),
     * added to the IoC container, and an [InjectedJDAEvent] will be fired.
     *
     * ### Custom event manager
     * You can provide your own [CoroutineEventManager] by using a service factory.
     *
     * @param event        The framework's ready event
     * @param eventManager The event manager from the (optional) [CoroutineEventManager] provider
     *
     */
    protected abstract fun createJDA(event: BReadyEvent, eventManager: IEventManager)

    @JvmSynthetic
    @BEventListener
    internal fun onReadyEvent(event: BReadyEvent, eventManager: IEventManager) {
        _eventManager = eventManager

        createJDA(event, eventManager)
    }

    /**
     * Creates a [JDABuilder with low memory profile settings][JDABuilder.createLight].
     *
     * In addition to the profile settings:
     * - The event manager is set to the (optional) [CoroutineEventManager]
     * - The intents are set to [JDAService.intents].
     * - In addition to the default-configured cache flags, [JDAService.cacheFlags] are added.
     * - The [REST Config][JDABuilder.setRestConfig] is set to [getDefaultRestConfig].
     *
     * You must not change intents, cache flags nor event manager using the builder.
     *
     * If you plan on growing your bot, prefer using [lightSharded] instead.
     */
    @CheckReturnValue
    fun light(token: String): JDABuilder {
        return JDABuilder.createLight(token, intents)
            .configureBase()
            .setRestConfig(getDefaultRestConfig())
    }

    /**
     * Creates a [JDABuilder with recommended default settings][JDABuilder.createDefault].
     *
     * In addition to the profile settings:
     * - The event manager is set to the (optional) [CoroutineEventManager]
     * - The intents are set to [JDAService.intents].
     * - In addition to the default-configured cache flags, [JDAService.cacheFlags] are added.
     * - The [REST Config][JDABuilder.setRestConfig] is set to [getDefaultRestConfig].
     *
     * You must not change intents, cache flags nor event manager using the builder.
     *
     * If you plan on growing your bot, prefer using [defaultSharded] instead.
     */
    @CheckReturnValue
    fun default(token: String): JDABuilder {
        return JDABuilder.createDefault(token, intents)
            .configureBase()
            .setRestConfig(getDefaultRestConfig())
    }

    /**
     * Creates a [JDABuilder with caches inferred from intents][JDABuilder.create].
     *
     * In addition to the profile settings:
     * - The event manager is set to the (optional) [CoroutineEventManager]
     * - The intents are set to [JDAService.intents].
     * - In addition to the default-configured cache flags, [JDAService.cacheFlags] are added.
     * - The [REST Config][JDABuilder.setRestConfig] is set to [getDefaultRestConfig].
     *
     * You must not change intents, cache flags nor event manager using the builder.
     *
     * If you plan on growing your bot, prefer using [createSharded] instead.
     */
    @CheckReturnValue
    fun create(token: String): JDABuilder {
        return JDABuilder.create(token, intents)
            .configureBase()
            .setRestConfig(getDefaultRestConfig())
    }

    @PublishedApi
    @JvmSynthetic
    internal fun JDABuilder.configureBase() = apply {
        setEventManager(eventManager)
        enableCache(cacheFlags)
    }

    /**
     * Creates a [DefaultShardManagerBuilder with low memory profile settings][DefaultShardManagerBuilder.createLight].
     *
     * In addition to the profile settings:
     * - The event manager is set to the (optional) [CoroutineEventManager]
     * - The intents are set to [JDAService.intents].
     * - In addition to the default-configured cache flags, [JDAService.cacheFlags] are added.
     * - The [REST Config][DefaultShardManagerBuilder.setRestConfig] is set to [getDefaultRestConfig].
     *
     * You must not change intents, cache flags nor event manager using the builder.
     */
    @CheckReturnValue
    fun lightSharded(token: String): DefaultShardManagerBuilder {
        return DefaultShardManagerBuilder.createLight(token, intents)
            .configureBase()
            .setRestConfig(getDefaultRestConfig())
    }

    /**
     * Creates a [DefaultShardManagerBuilder with recommended default settings][DefaultShardManagerBuilder.createDefault].
     *
     * In addition to the profile settings:
     * - The event manager is set to the (optional) [CoroutineEventManager]
     * - The intents are set to [JDAService.intents].
     * - In addition to the default-configured cache flags, [JDAService.cacheFlags] are added.
     * - The [REST Config][DefaultShardManagerBuilder.setRestConfig] is set to [getDefaultRestConfig].
     *
     * You must not change intents, cache flags nor event manager using the builder.
     */
    @CheckReturnValue
    fun defaultSharded(token: String): DefaultShardManagerBuilder {
        return DefaultShardManagerBuilder.createDefault(token, intents)
            .configureBase()
            .setRestConfig(getDefaultRestConfig())
    }

    /**
     * Creates a [DefaultShardManagerBuilder with caches inferred from intents][DefaultShardManagerBuilder.create].
     *
     * In addition to the DefaultShardManagerBuilder profile settings:
     * - The event manager is set to the (optional) [CoroutineEventManager]
     * - The intents are set to [JDAService.intents].
     * - In addition to the default-configured cache flags, [JDAService.cacheFlags] are added.
     * - The [REST Config][DefaultShardManagerBuilder.setRestConfig] is set to [getDefaultRestConfig].
     *
     * You must not change intents, cache flags nor event manager using the builder.
     */
    @CheckReturnValue
    fun createSharded(token: String): DefaultShardManagerBuilder {
        return DefaultShardManagerBuilder.create(token, intents)
            .configureBase()
            .setRestConfig(getDefaultRestConfig())
    }

    @PublishedApi
    @JvmSynthetic
    internal fun DefaultShardManagerBuilder.configureBase() = apply {
        setEventManagerProvider { eventManager }
        enableCache(cacheFlags)
    }

    companion object {
        /**
         * Returns the default JDA intents.
         *
         * Use [defaultIntents] to combine additional intents.
         *
         * @see GatewayIntent.DEFAULT
         */
        @JvmStatic
        val defaultIntents: EnumSet<GatewayIntent>
            get() = GatewayIntent.getIntents(GatewayIntent.DEFAULT)

        /**
         * Returns the default JDA intents, in addition to the provided intents.
         *
         * @see GatewayIntent.DEFAULT
         */
        @JvmStatic
        fun defaultIntents(vararg additionalIntents: GatewayIntent): EnumSet<GatewayIntent> {
            val intents = defaultIntents
            intents.addAll(additionalIntents)
            return intents
        }

        /**
         * Returns a [RestConfig] which uses the [default REST rate limiter][getDefaultRestRateLimiter].
         */
        @JvmStatic
        fun getDefaultRestConfig(): RestConfig {
            return RestConfig().setRateLimiterFactory(::getDefaultRestRateLimiter)
        }

        /**
         * Returns a [PriorityGlobalRestRateLimiter] with 50 requests/s,
         * using a [SequentialRestRateLimiter] as its [delegate][PriorityGlobalRestRateLimiter.delegate].
         */
        @JvmStatic
        fun getDefaultRestRateLimiter(rlConfig: RateLimitConfig): RestRateLimiter {
            return PriorityGlobalRestRateLimiter(tokens = 50, SequentialRestRateLimiter(rlConfig))
        }
    }
}

/**
 * Creates a [JDABuilder with low memory profile settings][JDABuilder.createLight].
 *
 * In addition to the profile settings:
 * - The event manager is set to the (optional) [CoroutineEventManager]
 * - The intents are set to [JDAService.intents].
 * - In addition to the default-configured cache flags, [JDAService.cacheFlags] are added.
 * - The [REST Config][JDABuilder.setRestConfig] is set to [getDefaultRestConfig].
 *
 * You must not change intents, cache flags nor event manager using the builder.
 *
 * If you plan on growing your bot, prefer using [lightSharded] instead.
 */
@JvmSynthetic
inline fun JDAService.light(
    token: String,
    memberCachePolicy: MemberCachePolicy? = null,
    chunkingFilter: ChunkingFilter? = null,
    activity: Activity? = null,
    restConfig: RestConfig? = getDefaultRestConfig(),
    block: JDABuilder.() -> Unit = {},
): JDA {
    return JDABuilder.createLight(token, intents)
        .configureBase()
        .configure(memberCachePolicy, chunkingFilter, activity, restConfig)
        .apply(block)
        .build()
}

/**
 * Creates a [JDABuilder with recommended default settings][JDABuilder.createDefault].
 *
 * In addition to the profile settings:
 * - The event manager is set to the (optional) [CoroutineEventManager]
 * - The intents are set to [JDAService.intents].
 * - In addition to the default-configured cache flags, [JDAService.cacheFlags] are added.
 * - The [REST Config][JDABuilder.setRestConfig] is set to [getDefaultRestConfig].
 *
 * You must not change intents, cache flags nor event manager using the builder.
 *
 * If you plan on growing your bot, prefer using [defaultSharded] instead.
 */
@JvmSynthetic
inline fun JDAService.default(
    token: String,
    memberCachePolicy: MemberCachePolicy? = null,
    chunkingFilter: ChunkingFilter? = null,
    activity: Activity? = null,
    restConfig: RestConfig? = getDefaultRestConfig(),
    block: JDABuilder.() -> Unit = {},
): JDA {
    return JDABuilder.createDefault(token, intents)
        .configureBase()
        .configure(memberCachePolicy, chunkingFilter, activity, restConfig)
        .apply(block)
        .build()
}

/**
 * Creates a [JDABuilder with caches inferred from intents][JDABuilder.create].
 *
 * In addition to the profile settings:
 * - The event manager is set to the (optional) [CoroutineEventManager]
 * - The intents are set to [JDAService.intents].
 * - In addition to the default-configured cache flags, [JDAService.cacheFlags] are added.
 * - The [REST Config][JDABuilder.setRestConfig] is set to [getDefaultRestConfig].
 *
 * You must not change intents, cache flags nor event manager using the builder.
 *
 * If you plan on growing your bot, prefer using [createSharded] instead.
 */
@JvmSynthetic
inline fun JDAService.create(
    token: String,
    memberCachePolicy: MemberCachePolicy? = null,
    chunkingFilter: ChunkingFilter? = null,
    activity: Activity? = null,
    restConfig: RestConfig? = getDefaultRestConfig(),
    block: JDABuilder.() -> Unit = {},
): JDA {
    return JDABuilder.create(token, intents)
        .configureBase()
        .configure(memberCachePolicy, chunkingFilter, activity, restConfig)
        .apply(block)
        .build()
}

@PublishedApi
@JvmSynthetic
internal fun JDABuilder.configure(
    memberCachePolicy: MemberCachePolicy?,
    chunkingFilter: ChunkingFilter?,
    activity: Activity?,
    restConfig: RestConfig?
) = apply {
    memberCachePolicy?.let(::setMemberCachePolicy)
    chunkingFilter?.let(::setChunkingFilter)
    setActivity(activity)

    if (restConfig != null) setRestConfig(restConfig)
}

/**
 * Creates a [DefaultShardManagerBuilder with low memory profile settings][DefaultShardManagerBuilder.createLight].
 *
 * In addition to the profile settings:
 * - The event manager is set to the (optional) [CoroutineEventManager]
 * - The intents are set to [JDAService.intents].
 * - In addition to the default-configured cache flags, [JDAService.cacheFlags] are added.
 * - The [REST Config][DefaultShardManagerBuilder.setRestConfig] is set to [getDefaultRestConfig].
 *
 * You must not change intents, cache flags nor event manager using the builder.
 */
@JvmSynthetic
inline fun JDAService.lightSharded(
    token: String,
    shardRange: IntRange? = null,
    shardsTotal: Int = -1,
    login: Boolean = true,
    memberCachePolicy: MemberCachePolicy? = null,
    chunkingFilter: ChunkingFilter? = null,
    noinline activityProvider: ((shardId: Int) -> Activity)? = null,
    restConfig: RestConfig? = getDefaultRestConfig(),
    block: DefaultShardManagerBuilder.() -> Unit = {}
): ShardManager {
    return DefaultShardManagerBuilder.createLight(token, intents)
        .configureBase()
        .configure(shardRange, shardsTotal, memberCachePolicy, chunkingFilter, activityProvider, restConfig)
        .apply(block)
        .build(login)
}

/**
 * Creates a [DefaultShardManagerBuilder with recommended default settings][DefaultShardManagerBuilder.createDefault].
 *
 * In addition to the profile settings:
 * - The event manager is set to the (optional) [CoroutineEventManager]
 * - The intents are set to [JDAService.intents].
 * - In addition to the default-configured cache flags, [JDAService.cacheFlags] are added.
 * - The [REST Config][DefaultShardManagerBuilder.setRestConfig] is set to [getDefaultRestConfig].
 *
 * You must not change intents, cache flags nor event manager using the builder.
 */
@JvmSynthetic
inline fun JDAService.defaultSharded(
    token: String,
    shardRange: IntRange? = null,
    shardsTotal: Int = -1,
    login: Boolean = true,
    memberCachePolicy: MemberCachePolicy? = null,
    chunkingFilter: ChunkingFilter? = null,
    noinline activityProvider: ((shardId: Int) -> Activity)? = null,
    restConfig: RestConfig? = getDefaultRestConfig(),
    block: DefaultShardManagerBuilder.() -> Unit = {}
): ShardManager {
    return DefaultShardManagerBuilder.createDefault(token, intents)
        .configureBase()
        .configure(shardRange, shardsTotal, memberCachePolicy, chunkingFilter, activityProvider, restConfig)
        .apply(block)
        .build(login)
}

/**
 * Creates a [DefaultShardManagerBuilder with caches inferred from intents][DefaultShardManagerBuilder.create].
 *
 * In addition to the DefaultShardManagerBuilder profile settings:
 * - The event manager is set to the (optional) [CoroutineEventManager]
 * - The intents are set to [JDAService.intents].
 * - In addition to the default-configured cache flags, [JDAService.cacheFlags] are added.
 * - The [REST Config][DefaultShardManagerBuilder.setRestConfig] is set to [getDefaultRestConfig].
 *
 * You must not change intents, cache flags nor event manager using the builder.
 */
@JvmSynthetic
inline fun JDAService.createSharded(
    token: String,
    shardRange: IntRange? = null,
    shardsTotal: Int = -1,
    login: Boolean = true,
    memberCachePolicy: MemberCachePolicy? = null,
    chunkingFilter: ChunkingFilter? = null,
    noinline activityProvider: ((shardId: Int) -> Activity)? = null,
    restConfig: RestConfig? = getDefaultRestConfig(),
    block: DefaultShardManagerBuilder.() -> Unit = {}
): ShardManager {
    return DefaultShardManagerBuilder.create(token, intents)
        .configureBase()
        .configure(shardRange, shardsTotal, memberCachePolicy, chunkingFilter, activityProvider, restConfig)
        .apply(block)
        .build(login)
}

@PublishedApi
@JvmSynthetic
internal fun DefaultShardManagerBuilder.configure(
    shardRange: IntRange?,
    shardsTotal: Int,
    memberCachePolicy: MemberCachePolicy?,
    chunkingFilter: ChunkingFilter?,
    activityProvider: ((Int) -> Activity)?,
    restConfig: RestConfig?
) = apply {
    setShardsTotal(shardsTotal)
    memberCachePolicy?.let(::setMemberCachePolicy)
    chunkingFilter?.let(::setChunkingFilter)
    setActivityProvider(activityProvider)

    if (shardRange != null) setShards(shardRange.first, shardRange.last)
    if (restConfig != null) setRestConfig(restConfig)
}