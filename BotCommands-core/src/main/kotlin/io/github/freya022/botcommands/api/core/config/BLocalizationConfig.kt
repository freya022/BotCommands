package io.github.freya022.botcommands.api.core.config

import io.github.freya022.botcommands.api.core.service.annotations.InjectedService
import io.github.freya022.botcommands.api.core.utils.toImmutableSet
import io.github.freya022.botcommands.api.localization.interaction.LocalizableInteraction
import io.github.freya022.botcommands.api.localization.providers.DefaultLocalizationMapProvider
import io.github.freya022.botcommands.api.localization.readers.JacksonLocalizationMapReader
import io.github.freya022.botcommands.internal.core.config.ConfigDSL
import io.github.freya022.botcommands.internal.core.config.ConfigurationValue

@InjectedService
interface BLocalizationConfig : IConfig, BLocalizationConfigProps {
    override val configType get() = BLocalizationConfig::class.java
}

interface BLocalizationConfigProps {
    /**
     * Localization bundles available for localizing interaction responses, with [LocalizableInteraction],
     * not to be confused with those used to [localize commands][BApplicationConfigBuilder.addLocalizations].
     *
     * As a reminder, the localization bundles are in `bc_localization` by default.
     *
     * For example: `MyCommandResponses` will, by default,
     * find bundles similar to `/bc_localization/MyCommands_<locale>.json`.
     *
     * See [DefaultLocalizationMapProvider] and [JacksonLocalizationMapReader] for default implementation details.
     *
     * Spring property: `botcommands.localization.responseBundles`
     *
     * @see BApplicationConfigBuilder.addLocalizations
     */
    @get:ConfigurationValue(
        path = "botcommands.localization.responseBundles",
        description = "Localization bundles available for localizing interaction responses, with [LocalizableInteraction]. See the documentation for more details.",
    )
    val responseBundles: Set<String>
}

@ConfigDSL
class BLocalizationConfigBuilder internal constructor() : BLocalizationConfigProps {
    override val responseBundles: MutableSet<String> = hashSetOf()

    /**
     * Adds a localization bundle for localizing interaction responses, with [LocalizableInteraction],
     * not to be confused with those used to [localize commands][BApplicationConfigBuilder.addLocalizations].
     *
     * As a reminder, the localization bundles are in `bc_localization` by default.
     *
     * For example: `MyCommandResponses` will, by default,
     * find bundles similar to `/bc_localization/MyCommands_<locale>.json`.
     *
     * See [DefaultLocalizationMapProvider] and [JacksonLocalizationMapReader] for default implementation details.
     *
     * Spring property: `botcommands.localization.responseBundles`
     *
     * @see BApplicationConfigBuilder.addLocalizations
     */
    fun addResponseBundle(responseBundle: String) {
        responseBundles += responseBundle
    }

    @JvmSynthetic
    internal fun build() = object : BLocalizationConfig {
        override val responseBundles: Set<String> = this@BLocalizationConfigBuilder.responseBundles.toImmutableSet()
    }
}
