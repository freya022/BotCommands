package com.freya02.botcommands.api.core.config

import com.freya02.botcommands.api.core.db.ConnectionSupplier
import com.freya02.botcommands.api.core.service.annotations.InjectedService
import com.freya02.botcommands.internal.core.config.ConfigDSL

@InjectedService
interface BComponentsConfig {
    /**
     * Allows loading component services.
     *
     * This requires a [ConnectionSupplier] service to be present
     *
     * Default: `false`
     *
     * @see ConnectionSupplier
     */
    val useComponents: Boolean
}

@ConfigDSL
class BComponentsConfigBuilder internal constructor() : BComponentsConfig {
    override var useComponents: Boolean = false

    @JvmSynthetic
    internal fun build() = object : BComponentsConfig {
        override val useComponents = this@BComponentsConfigBuilder.useComponents
    }
}