package io.github.freya022.botcommands.api.core.config

import io.github.freya022.botcommands.api.components.Buttons
import io.github.freya022.botcommands.api.components.SelectMenus
import io.github.freya022.botcommands.api.components.annotations.RequiresComponents
import io.github.freya022.botcommands.api.core.db.ConnectionSupplier
import io.github.freya022.botcommands.api.core.service.annotations.InjectedService
import io.github.freya022.botcommands.internal.core.config.ConfigDSL
import io.github.freya022.botcommands.internal.core.config.ConfigurationValue

@InjectedService
interface BComponentsConfig : IConfig, BComponentsConfigProps {

    override val configType get() = BComponentsConfig::class.java
}

interface BComponentsConfigProps {

    /**
     * Whether the components feature should be enabled. Enabling this requires a [ConnectionSupplier] service.
     *
     * You can use [@RequiresComponents][RequiresComponents]
     * to disable services when this is set to `false`.
     *
     * Default: `false`
     *
     * Spring property: `botcommands.components.enable` ; Spring property takes over this config property.
     *
     * @see ConnectionSupplier
     * @see Buttons
     * @see SelectMenus
     */
    @get:ConfigurationValue(
        path = "botcommands.components.enable",
        description = "Whether the components feature should be enabled. Enabling this requires a [ConnectionSupplier] service.",
        defaultValue = "false",
    )
    val enable: Boolean
}

@ConfigDSL
class BComponentsConfigBuilder internal constructor() : BComponentsConfigProps {

    @set:JvmName("enable")
    override var enable: Boolean = false

    @JvmSynthetic
    internal fun build() = object : BComponentsConfig {
        override val enable: Boolean = this@BComponentsConfigBuilder.enable
    }
}
