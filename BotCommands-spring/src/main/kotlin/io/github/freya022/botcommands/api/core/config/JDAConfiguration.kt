package io.github.freya022.botcommands.api.core.config

import io.github.freya022.botcommands.api.core.JDAService
import io.github.freya022.botcommands.internal.core.config.ConfigurationValue
import io.github.freya022.botcommands.internal.core.config.IgnoreDefaultValue
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.cache.CacheFlag
import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Configuration properties for [JDAService].
 */
@ConfigurationProperties(prefix = "jda")
class JDAConfiguration internal constructor(
    /**
     * The intents for each shard.
     *
     * This does not apply automatically, this is only here for your convenience.
     *
     * Default: [JDAService.defaultIntents]
     *
     * Spring property: `jda.intents`
     */
    @get:IgnoreDefaultValue
    @get:ConfigurationValue(
        path = "jda.intents",
        description = "The intents for each shard. This does not apply automatically, this is only here for your convenience.",
    )
    val intents: Set<GatewayIntent> = JDAService.defaultIntents,
    /**
     * The cache flags for each shard.
     *
     * This does not apply automatically, this is only here for your convenience.
     *
     * Default: None
     *
     * Spring property: `jda.intents`
     */
    @get:IgnoreDefaultValue
    @get:ConfigurationValue(
        path = "jda.cacheFlags",
        description = "The cache flags for each shard. This does not apply automatically, this is only here for your convenience.",
    )
    val cacheFlags: Set<CacheFlag> = emptySet(),
)
