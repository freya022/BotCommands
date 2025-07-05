package io.github.freya022.botcommands.api.core.config

import io.github.freya022.botcommands.api.core.service.annotations.InjectedService
import io.github.freya022.botcommands.api.modals.annotations.RequiresModals
import io.github.freya022.botcommands.internal.core.config.ConfigDSL
import io.github.freya022.botcommands.internal.core.config.ConfigurationValue

@InjectedService
interface BModalsConfig {
    /**
     * Whether modal interactions should be listened for.
     *
     * You can use [@RequiresModals][RequiresModals] to disable services when this is set to `false`.
     *
     * Default: `true`
     *
     * Spring property: `botcommands.modals.enable`
     */
    @ConfigurationValue(path = "botcommands.modals.enable", defaultValue = "true")
    val enable: Boolean
}

@ConfigDSL
class BModalsConfigBuilder internal constructor() : BModalsConfig {
    @set:JvmName("enable")
    override var enable: Boolean = true

    @JvmSynthetic
    internal fun build() = object : BModalsConfig {
        override val enable = this@BModalsConfigBuilder.enable
    }
}
