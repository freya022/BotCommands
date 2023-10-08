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
 *     override fun createJDA(event: BReadyEvent, eventManager: IEventManager) {
 *         DefaultShardManagerBuilder.createLight(config.token).apply {
 *             setEventManagerProvider { eventManager }
 *             enableIntents(intents)
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
     * The intents used by your bot.
     */
    abstract val intents: Set<GatewayIntent>

    /**
     * Creates a [JDA] or [ShardManager] instance.
     *
     * The framework will pick up the JDA instance (or one of its shards) automatically,
     * but for that you **need** to use the provided [eventManager] in either:
     * - [jda.setEventManager(eventManager)][JDA.setEventManager]
     * - [shardManagerBuilder.setEventManagerProvider { eventManager }][DefaultShardManagerBuilder.setEventManagerProvider]
     *
     * @param event        the framework's ready event
     * @param eventManager the event manager passed to [BBuilder.newBuilder], you **must** use it in your [JDABuilder]/[DefaultShardManagerBuilder]
     *
     */
    abstract fun createJDA(event: BReadyEvent, eventManager: IEventManager)

    @JvmSynthetic
    @BEventListener
    internal fun onReadyEvent(event: BReadyEvent, eventManager: IEventManager) = createJDA(event, eventManager)

    companion object {
        @JvmStatic
        val defaultIntents: EnumSet<GatewayIntent>
            get() = GatewayIntent.getIntents(GatewayIntent.DEFAULT)
    }
}