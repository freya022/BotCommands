package io.github.freya022.botcommands.api.core.config

import io.github.freya022.botcommands.api.core.service.annotations.InjectedService
import io.github.freya022.botcommands.api.emojis.AppEmojisRegistry
import io.github.freya022.botcommands.internal.core.config.ConfigDSL
import io.github.freya022.botcommands.internal.core.config.ConfigurationValue

@InjectedService
interface BAppEmojisConfig : IConfig, BAppEmojisConfigProps {
    override val configType get() = BAppEmojisConfig::class.java
}

interface BAppEmojisConfigProps {
    /**
     * Allows uploading application emojis at startup, and retrieving them from [AppEmojisRegistry].
     *
     * Default: `false`
     *
     * Spring property: `botcommands.app.emojis.enable`
     */
    @ConfigurationValue(path = "botcommands.app.emojis.enable", defaultValue = "false")
    val enable: Boolean

    /**
     * Allows deleting application emojis that are not managed by this application,
     * starting from the oldest.
     *
     * **Note:** Emojis with the same name on Discord, but with a different image, will not be deleted.
     *
     * Default: `false`
     *
     * Spring property: `botcommands.app.emojis.deleteOnOutOfSlots`
     */
    @ConfigurationValue(path = "botcommands.app.emojis.deleteOnOutOfSlots", defaultValue = "false")
    val deleteOnOutOfSlots: Boolean
}

@ConfigDSL
class BAppEmojisConfigBuilder internal constructor() : BAppEmojisConfigProps {
    @set:JvmName("enable")
    override var enable: Boolean = false

    @set:JvmName("deleteOnOutOfSlots")
    override var deleteOnOutOfSlots: Boolean = false

    @JvmSynthetic
    internal fun build() = object : BAppEmojisConfig {
        override val enable: Boolean = this@BAppEmojisConfigBuilder.enable
        override val deleteOnOutOfSlots = this@BAppEmojisConfigBuilder.deleteOnOutOfSlots
    }
}
