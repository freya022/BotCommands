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
    val enable: Boolean

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
    @Deprecated("Replaced by 'enable'", ReplaceWith("enable"))
    val useComponents: Boolean get() = enable
}

@ConfigDSL
class BComponentsConfigBuilder internal constructor() : BComponentsConfig {
    @set:JvmName("enable")
    override var enable: Boolean = false

    @Deprecated("Replaced by 'enable'", replaceWith = ReplaceWith("enable"))
    @set:JvmName("useComponents")
    override var useComponents: Boolean
        get() = enable
        set(value) {
            enable = value
        }

    @JvmSynthetic
    internal fun build() = object : BComponentsConfig {
        override val enable: Boolean = this@BComponentsConfigBuilder.enable
    }
}