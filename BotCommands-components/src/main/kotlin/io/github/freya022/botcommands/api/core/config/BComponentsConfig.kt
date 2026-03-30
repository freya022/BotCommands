package io.github.freya022.botcommands.api.core.config

import io.github.freya022.botcommands.api.core.service.annotations.InjectedService
import io.github.freya022.botcommands.internal.core.config.ConfigDSL

/**
 * Configuration for the components feature.
 *
 * A configuration of this feature must be registered for it to be active.
 *
 * Spring users can set the `botcommands.components.enable` property to `false` to disable this feature,
 * as adding the dependency will enable it by default.
 *
 * @see [BComponentsConfig.builder]
 * @see [registerComponents]
 */
@InjectedService
interface BComponentsConfig : IConfig, BComponentsConfigProps {

    override val configType get() = BComponentsConfig::class.java

    companion object {
        /**
         * Creates a new [BComponentsConfigBuilder], you must [build][BComponentsConfigBuilder.build] it and [register][BConfigBuilder.registerModule] it.
         */
        @JvmStatic
        fun builder(): BComponentsConfigBuilder {
            return BComponentsConfigBuilder.create()
        }
    }
}

interface BComponentsConfigProps {
}

/**
 * Builder of [BComponentsConfig].
 *
 * @see BComponentsConfig.builder
 */
@ConfigDSL
class BComponentsConfigBuilder private constructor() : BComponentsConfigProps {

    /**
     * Builds the [BComponentsConfig], you can register the built configuration with [BConfigBuilder.registerModule].
     */
    fun build() = object : BComponentsConfig {
    }

    internal companion object {
        @JvmSynthetic
        internal fun create(): BComponentsConfigBuilder = BComponentsConfigBuilder()
    }
}

/**
 * Registers the components feature.
 *
 * @param block A block for further configuration
 *
 * @see BComponentsConfig
 */
fun BConfigBuilder.registerComponents(block: BComponentsConfigBuilder.() -> Unit = { }) {
    val config = BComponentsConfigBuilder.create()
        .apply(block)
        .build()
    registerModule(config)
}
