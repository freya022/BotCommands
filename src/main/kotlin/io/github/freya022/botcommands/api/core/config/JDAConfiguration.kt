@file:Suppress("ConfigurationProperties")

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
    val cacheFlags: Set<CacheFlag> = emptySet()
)