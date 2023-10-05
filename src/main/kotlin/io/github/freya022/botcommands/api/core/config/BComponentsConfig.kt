package io.github.freya022.botcommands.api.core.config

import io.github.freya022.botcommands.api.core.db.ConnectionSupplier
import io.github.freya022.botcommands.api.core.service.annotations.InjectedService
import io.github.freya022.botcommands.internal.core.config.ConfigDSL

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
    @set:JvmName("useComponents")
    override var useComponents: Boolean = false

    @JvmSynthetic
    internal fun build() = object : BComponentsConfig {
        override val useComponents = this@BComponentsConfigBuilder.useComponents
    }
}