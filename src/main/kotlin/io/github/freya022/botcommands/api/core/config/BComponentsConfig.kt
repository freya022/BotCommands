package io.github.freya022.botcommands.api.core.config

import io.github.freya022.botcommands.api.components.Buttons
import io.github.freya022.botcommands.api.components.Components
import io.github.freya022.botcommands.api.components.SelectMenus
import io.github.freya022.botcommands.api.core.db.ConnectionSupplier
import io.github.freya022.botcommands.api.core.service.annotations.InjectedService
import io.github.freya022.botcommands.internal.core.config.ConfigDSL
import io.github.freya022.botcommands.internal.core.config.ConfigurationValue

@InjectedService
interface BComponentsConfig {
    /**
     * Allows loading component services,
     * such as [Components], [Buttons] and [SelectMenus].
     *
     * This requires a [ConnectionSupplier] service to be present
     *
     * Default: `false`
     *
     * Spring property: `botcommands.components.enable` ; Spring property takes over this config property.
     *
     * @see ConnectionSupplier
     */
    @ConfigurationValue(path = "botcommands.components.enable", defaultValue = "false")
    val useComponents: Boolean
}

@ConfigDSL
class BComponentsConfigBuilder internal constructor() : BComponentsConfig {
    @set:JvmName("useComponents")
    override var useComponents: Boolean = false

    @JvmSynthetic
    internal fun build() = object : BComponentsConfig {
        override val useComponents = this@BComponentsConfigBuilder.useComponents
    }
}