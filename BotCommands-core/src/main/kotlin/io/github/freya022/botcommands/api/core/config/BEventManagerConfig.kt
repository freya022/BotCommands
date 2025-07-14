package io.github.freya022.botcommands.api.core.config

import io.github.freya022.botcommands.api.core.annotations.BEventListener
import io.github.freya022.botcommands.api.core.service.annotations.InjectedService
import io.github.freya022.botcommands.internal.core.config.ConfigDSL
import io.github.freya022.botcommands.internal.core.config.ConfigurationValue
import java.time.Duration as JavaDuration
import kotlin.time.Duration
import kotlin.time.toJavaDuration
import kotlin.time.toKotlinDuration

@InjectedService
interface BEventManagerConfig {

    /**
     * The time applied to all event listeners by default before their coroutine is cancelled.
     *
     * A non-positive or non-finite value equals to no timeout.
     *
     * Default: `Duration.INFINITE`
     *
     * Spring property: `botcommands.event.manager.defaultTimeout`
     *
     * @see BEventListener.timeout
     */
    @ConfigurationValue(path = "botcommands.event.manager.defaultTimeout", type = "java.time.Duration")
    val defaultTimeout: Duration

    fun getDefaultTimeout(): JavaDuration = defaultTimeout.toJavaDuration()
}

@ConfigDSL
class BEventManagerConfigBuilder internal constructor() : BEventManagerConfig {

    override var defaultTimeout: Duration = Duration.INFINITE

    fun setDefaultTimeout(timeout: JavaDuration) {
        defaultTimeout = timeout.toKotlinDuration()
    }

    @JvmSynthetic
    internal fun build() = object : BEventManagerConfig {
        override val defaultTimeout = this@BEventManagerConfigBuilder.defaultTimeout
    }
}
