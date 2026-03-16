package io.github.freya022.botcommands.api.core.config

import io.github.freya022.botcommands.api.core.service.annotations.InjectedService
import io.github.freya022.botcommands.api.modals.annotations.RequiresModals
import io.github.freya022.botcommands.internal.core.config.ConfigDSL
import io.github.freya022.botcommands.internal.core.config.ConfigurationValue

@InjectedService
interface BModalsConfig : IConfig, BModalsConfigProps {
    override val configType get() = BModalsConfig::class.java
}

interface BModalsConfigProps {
    /**
     * Whether the modals feature should be enabled.
     *
     * You can use [@RequiresModals][RequiresModals] to disable services when this is set to `false`.
     *
     * Default: `true`
     *
     * Spring property: `botcommands.modals.enable`
     */
    @get:ConfigurationValue(
        path = "botcommands.modals.enable",
        description = "Whether the modals feature should be enabled.",
        defaultValue = "true",
    )
    val enable: Boolean
}

@ConfigDSL
class BModalsConfigBuilder internal constructor() : BModalsConfigProps {
    @set:JvmName("enable")
    override var enable: Boolean = true

    @JvmSynthetic
    internal fun build() = object : BModalsConfig {
        override val enable = this@BModalsConfigBuilder.enable
    }
}
