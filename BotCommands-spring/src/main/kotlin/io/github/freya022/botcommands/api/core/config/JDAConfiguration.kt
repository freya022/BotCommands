package io.github.freya022.botcommands.api.core.config

import io.github.freya022.botcommands.api.core.JDAService
import io.github.freya022.botcommands.internal.core.config.ConfigurationValue
import io.github.freya022.botcommands.internal.core.config.DeprecatedValue
import io.github.freya022.botcommands.internal.core.config.IgnoreDefaultValue
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.cache.CacheFlag
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.Name
import org.springframework.context.event.ContextClosedEvent
import java.time.Duration as JavaDuration
import kotlin.time.Duration
import kotlin.time.toKotlinDuration

/**
 * Configuration properties for [JDAService].
 */
@ConfigurationProperties(prefix = "jda")
class JDAConfiguration internal constructor(
    /**
     * The intents for each shard.
     *
     * Default: [JDAService.defaultIntents]
     *
     * Spring property: `jda.intents`
     */
    @IgnoreDefaultValue
    @ConfigurationValue("jda.intents")
    val intents: Set<GatewayIntent> = JDAService.defaultIntents,
    /**
     * The cache flags for each shard.
     *
     * Default: None
     *
     * Spring property: `jda.intents`
     */
    @IgnoreDefaultValue
    @ConfigurationValue("jda.cacheFlags")
    val cacheFlags: Set<CacheFlag> = emptySet(),
    @Suppress("DEPRECATION")
    @param:Name("devtools")
    @Deprecated("For removal")
    val devTools: DevTools = DevTools(),
) {

    @Deprecated("For removal")
    class DevTools internal constructor(
        /**
         * When Spring devtools are enabled,
         * enables shutting down JDA when the IoC container [closes][ContextClosedEvent].
         *
         * If you disable this, you must shut down JDA manually,
         * not doing so will let old instances run, receive events and cause unwanted behavior.
         *
         * Default: `true`
         */
        @ConfigurationValue("jda.devtools.enabled", defaultValue = "true")
        @Deprecated("Replaced by the built-in shutdown hook")
        @DeprecatedValue("Replaced by the built-in shutdown hook", replacement = "botcommands.core.enableShutdownHook")
        val enabled: Boolean = true,
        shutdownTimeout: JavaDuration = JavaDuration.ofSeconds(10),
    ) {
        /**
         * Time to wait until JDA needs to be forcefully shut down,
         * in other words, this is the allowed time for a graceful shutdown.
         */
        @Deprecated("For removal, shutdown is always forced")
        @DeprecatedValue("For removal, shutdown is always forced")
        @ConfigurationValue("jda.devtools.shutdownTimeout", type = "java.time.Duration", defaultValue = "10s")
        val shutdownTimeout: Duration = shutdownTimeout.toKotlinDuration()
    }
}
