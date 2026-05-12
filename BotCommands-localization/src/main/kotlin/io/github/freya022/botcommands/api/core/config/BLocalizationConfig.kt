package io.github.freya022.botcommands.api.core.config

import io.github.freya022.botcommands.api.core.service.annotations.InjectedService
import io.github.freya022.botcommands.api.core.utils.toImmutableSet
import io.github.freya022.botcommands.api.localization.interaction.LocalizableInteraction
import io.github.freya022.botcommands.api.localization.providers.DefaultLocalizationMapProvider
import io.github.freya022.botcommands.api.localization.readers.JacksonLocalizationMapReader
import io.github.freya022.botcommands.internal.core.config.ConfigDSL
import io.github.freya022.botcommands.internal.core.config.ConfigurationValue

/**
 * Configuration for the localization feature.
 *
 * @see [BLocalizationConfig.builder]
 * @see [registerLocalization]
 */
@InjectedService
interface BLocalizationConfig : IConfig, BLocalizationConfigProps {
    override val configType get() = BLocalizationConfig::class.java

    companion object {
        /**
         * Creates a new [BLocalizationConfigBuilder], you must [build][BLocalizationConfigBuilder.build] it and [register][BConfigBuilder.registerModule] it.
         */
        @JvmStatic
        fun builder(): BLocalizationConfigBuilder {
            return BLocalizationConfigBuilder.create()
        }
    }
}

interface BLocalizationConfigProps {
    /**
     * Localization bundles available for localizing interaction responses, with [LocalizableInteraction],
     * not to be confused with those used to localize commands, available in the application command module's configuration.
     *
     * As a reminder, the localization bundles are in `bc_localization` by default.
     *
     * For example: `MyCommandResponses` will, by default,
     * find bundles similar to `/bc_localization/MyCommands_<locale>.json`.
     *
     * See [DefaultLocalizationMapProvider] and [JacksonLocalizationMapReader] for default implementation details.
     *
     * Spring property: `botcommands.localization.responseBundles`
     */
    @get:ConfigurationValue(
        path = "botcommands.localization.responseBundles",
        description = "Localization bundles available for localizing interaction responses, with [LocalizableInteraction]. See the documentation for more details.",
    )
    val responseBundles: Set<String>
}

/**
 * Builder of [BLocalizationConfig].
 *
 * @see BLocalizationConfig.builder
 */
@ConfigDSL
class BLocalizationConfigBuilder private constructor() : BLocalizationConfigProps {
    override val responseBundles: MutableSet<String> = hashSetOf()

    /**
     * Adds a localization bundle for localizing interaction responses, with [LocalizableInteraction],
     * not to be confused with those used to localize commands, available in the application command module's configuration.
     *
     * As a reminder, the localization bundles are in `bc_localization` by default.
     *
     * For example: `MyCommandResponses` will, by default,
     * find bundles similar to `/bc_localization/MyCommands_<locale>.json`.
     *
     * See [DefaultLocalizationMapProvider] and [JacksonLocalizationMapReader] for default implementation details.
     *
     * Spring property: `botcommands.localization.responseBundles`
     */
    fun addResponseBundle(responseBundle: String) {
        responseBundles += responseBundle
    }

    fun build() = object : BLocalizationConfig {
        override val responseBundles: Set<String> = this@BLocalizationConfigBuilder.responseBundles.toImmutableSet()
    }

    internal companion object {
        @JvmSynthetic
        internal fun create(): BLocalizationConfigBuilder = BLocalizationConfigBuilder()
    }
}

/**
 * Registers the localization feature.
 *
 * @param block A block for further configuration
 *
 * @see BLocalizationConfig
 */
fun BConfigBuilder.registerLocalization(block: BLocalizationConfigBuilder.() -> Unit = { }) {
    val config = BLocalizationConfigBuilder.create()
        .apply(block)
        .build()
    registerModule(config)
}
