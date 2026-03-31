package io.github.freya022.botcommands.api.core.config

import io.github.freya022.botcommands.api.core.service.annotations.InjectedService
import io.github.freya022.botcommands.internal.core.config.ConfigDSL

/**
 * Configuration for the modals feature.
 *
 * A configuration of this feature must be registered for it to be active.
 *
 * Spring users can set the `botcommands.modals.enable` property to `false` to disable this feature,
 * as adding the dependency will enable it by default.
 *
 * @see [BModalsConfig.builder]
 * @see [registerModals]
 */
@InjectedService
interface BModalsConfig : IConfig, BModalsConfigProps {
    override val configType get() = BModalsConfig::class.java

    companion object {
        /**
         * Creates a new [BModalsConfigBuilder], you must [build][BModalsConfigBuilder.build] it and [register][BConfigBuilder.registerModule] it.
         */
        @JvmStatic
        fun builder(): BModalsConfigBuilder {
            return BModalsConfigBuilder.create()
        }
    }
}

interface BModalsConfigProps {
}

/**
 * Builder of [BModalsConfig].
 *
 * @see BModalsConfig.builder
 */
@ConfigDSL
class BModalsConfigBuilder private constructor() : BModalsConfigProps {

    fun build() = object : BModalsConfig {
    }

    internal companion object {
        @JvmSynthetic
        internal fun create(): BModalsConfigBuilder = BModalsConfigBuilder()
    }
}

/**
 * Registers the modals feature.
 *
 * @param block A block for further configuration
 *
 * @see BModalsConfig
 */
fun BConfigBuilder.registerModals(block: BModalsConfigBuilder.() -> Unit = { }) {
    val config = BModalsConfigBuilder.create()
        .apply(block)
        .build()
    registerModule(config)
}
