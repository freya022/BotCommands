package io.github.freya022.botcommands.api.core.config

import io.github.freya022.botcommands.api.core.service.annotations.InjectedService
import io.github.freya022.botcommands.api.emojis.annotations.RequiresAppEmojis
import io.github.freya022.botcommands.internal.core.config.ConfigDSL
import io.github.freya022.botcommands.internal.core.config.ConfigurationValue

/**
 * Configuration for the app emojis feature.
 *
 * A configuration of this feature must be registered for it to be active.
 *
 * Spring users can set the `botcommands.app.emojis.enable` property to `false` to disable this feature,
 * as adding the dependency will enable it by default.
 *
 * [@RequiresAppEmojis][RequiresAppEmojis] can be used to disable services when this feature isn't registered.
 *
 * @see [BAppEmojisConfig.builder]
 * @see [registerAppEmojis]
 */
@InjectedService
interface BAppEmojisConfig : IConfig, BAppEmojisConfigProps {

    override val configType get() = BAppEmojisConfig::class.java

    companion object {
        /**
         * Creates a new [BAppEmojisConfigBuilder], you must [build][BAppEmojisConfigBuilder.build] it and [register][BConfigBuilder.registerModule] it.
         */
        @JvmStatic
        fun builder(): BAppEmojisConfigBuilder {
            return BAppEmojisConfigBuilder.create()
        }
    }
}

interface BAppEmojisConfigProps {

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
    @get:ConfigurationValue(
        path = "botcommands.app.emojis.deleteOnOutOfSlots",
        description = "Allows deleting application emojis that are not managed by this application, starting from the oldest. This keeps emojis by name, not by content.",
        defaultValue = "false",
    )
    val deleteOnOutOfSlots: Boolean
}

/**
 * Builder of [BAppEmojisConfig].
 *
 * @see BAppEmojisConfig.builder
 */
@ConfigDSL
class BAppEmojisConfigBuilder private constructor() : BAppEmojisConfigProps {

    @set:JvmName("deleteOnOutOfSlots")
    override var deleteOnOutOfSlots: Boolean = false

    fun build() = object : BAppEmojisConfig {
        override val deleteOnOutOfSlots = this@BAppEmojisConfigBuilder.deleteOnOutOfSlots
    }

    internal companion object {
        @JvmSynthetic
        internal fun create(): BAppEmojisConfigBuilder = BAppEmojisConfigBuilder()
    }
}

/**
 * Registers the app emojis feature.
 *
 * @param block A block for further configuration
 *
 * @see BAppEmojisConfig
 */
fun BConfigBuilder.registerAppEmojis(block: BAppEmojisConfigBuilder.() -> Unit = { }) {
    val config = BAppEmojisConfigBuilder.create()
        .apply(block)
        .build()
    registerModule(config)
}
