package dev.freya02.botcommands.jda.keepalive.api.config

import dev.freya02.botcommands.jda.keepalive.api.ExperimentalKeepAliveApi
import dev.freya02.botcommands.jda.keepalive.internal.exceptions.throwInternal
import io.github.freya022.botcommands.api.core.config.BConfig
import io.github.freya022.botcommands.api.core.config.BConfigBuilder
import io.github.freya022.botcommands.api.core.config.IConfig
import io.github.freya022.botcommands.api.core.config.getConfigOrNull
import io.github.freya022.botcommands.api.core.service.annotations.InjectedService
import io.github.freya022.botcommands.internal.core.config.ConfigDSL

@ExperimentalKeepAliveApi
interface JDAKeepAliveConfigProps {
    // TODO document how to add agent (link wiki), fallback to dynamic loading
    // TODO do we actually need a user supplied key? we can use UUIDs instead, but we need to keep the key, which should be possible as BC's modules shouldn't get unloaded
    val cacheKey: String
}

@InjectedService
@ExperimentalKeepAliveApi
interface JDAKeepAliveConfig : IConfig, JDAKeepAliveConfigProps {
    override val configType get() = JDAKeepAliveConfig::class.java
}

internal val BConfig.jdaKeepAliveConfig: JDAKeepAliveConfig
    get() = getConfigOrNull<JDAKeepAliveConfig>()
        ?: throwInternal("Attempted to fetch a configuration of a disabled feature")

@ConfigDSL
@ExperimentalKeepAliveApi
class JDAKeepAliveConfigBuilder private constructor(
    override val cacheKey: String,
) : JDAKeepAliveConfigProps {

    fun build(): JDAKeepAliveConfig = object : JDAKeepAliveConfig {
        override val cacheKey = this@JDAKeepAliveConfigBuilder.cacheKey
    }

    companion object {
        @JvmStatic
        fun create(cacheKey: String): JDAKeepAliveConfigBuilder {
            return JDAKeepAliveConfigBuilder(cacheKey)
        }
    }
}

@ExperimentalKeepAliveApi
fun BConfigBuilder.withJDAKeepAlive(cacheKey: String, block: JDAKeepAliveConfigBuilder.() -> Unit = { }) {
    val config = JDAKeepAliveConfigBuilder.create(cacheKey)
        .apply(block)
        .build()
    withConfig(config)
}
