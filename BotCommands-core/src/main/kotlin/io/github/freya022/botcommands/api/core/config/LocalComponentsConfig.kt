package io.github.freya022.botcommands.api.core.config

import io.github.freya022.botcommands.api.core.service.annotations.InjectedService
import io.github.freya022.botcommands.internal.core.config.ConfigDSL
import io.github.freya022.botcommands.internal.utils.throwInternal

/**
 * Configuration for the local components feature.
 *
 * A configuration of this feature must be registered for it to be active.
 *
 * Spring users must set the `botcommands.local-components.enable` property to `true` to enable this feature.
 *
 * @see [LocalComponentsConfig.builder]
 * @see [registerLocalComponents]
 */
@InjectedService
interface LocalComponentsConfig : IConfig, LocalComponentsConfigProps {

    override val configType get() = LocalComponentsConfig::class.java

    companion object {
        /**
         * Creates a new [LocalComponentsConfigBuilder], you must [build][LocalComponentsConfigBuilder.build] it and [register][BConfigBuilder.registerModule] it.
         */
        @JvmStatic
        fun builder(): LocalComponentsConfigBuilder {
            return LocalComponentsConfigBuilder.create()
        }
    }
}

internal val BConfig.localComponents: LocalComponentsConfig
    get() = getConfigOrNull<LocalComponentsConfig>()
        ?: throwInternal("Attempted to fetch a configuration of a disabled feature")

interface LocalComponentsConfigProps {
}

@ConfigDSL
class LocalComponentsConfigBuilder private constructor() : LocalComponentsConfigProps {

    /**
     * Builds the [LocalComponentsConfig], you can register the built configuration with [BConfigBuilder.registerModule].
     */
    fun build(): LocalComponentsConfig = object : LocalComponentsConfig {
    }

    internal companion object {
        internal fun create(): LocalComponentsConfigBuilder = LocalComponentsConfigBuilder()
    }
}

/**
 * Registers the local components feature.
 *
 * @param block A block for further configuration
 *
 * @see LocalComponentsConfig
 */
fun BConfigBuilder.registerLocalComponents(block: LocalComponentsConfigBuilder.() -> Unit = { }) {
    val config = LocalComponentsConfigBuilder.create()
        .apply(block)
        .build()
    registerModule(config)
}
