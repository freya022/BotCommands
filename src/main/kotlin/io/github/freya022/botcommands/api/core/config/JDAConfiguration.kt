@file:Suppress("ConfigurationProperties")

package io.github.freya022.botcommands.api.core.config

import io.github.freya022.botcommands.api.core.JDAService
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.cache.CacheFlag
import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Configuration properties for [JDAService].
 */
@ConfigurationProperties(prefix = "jda")
class JDAConfiguration internal constructor(
    val intents: Set<GatewayIntent> = JDAService.defaultIntents,
    val cacheFlags: Set<CacheFlag> = emptySet()
)