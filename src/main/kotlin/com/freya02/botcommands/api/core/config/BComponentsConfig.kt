package com.freya02.botcommands.api.core.config

import com.freya02.botcommands.api.components.ComponentInteractionFilter
import com.freya02.botcommands.api.core.db.ConnectionSupplier
import com.freya02.botcommands.api.core.service.annotations.InjectedService
import com.freya02.botcommands.internal.core.config.ConfigDSL
import com.freya02.botcommands.internal.toImmutableList

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

    /**
     * Filters for the component interaction listener, they will check all components such as buttons and selection menus
     *
     * If one of the filters returns `false`, then the component's code is not executed
     *
     * **You still have to acknowledge to the interaction !**
     */
    val componentFilters: List<ComponentInteractionFilter>
}

@ConfigDSL
class BComponentsConfigBuilder internal constructor() : BComponentsConfig {
    override var useComponents: Boolean = false
    override val componentFilters: MutableList<ComponentInteractionFilter> = arrayListOf()

    @JvmSynthetic
    internal fun build() = object : BComponentsConfig {
        override val useComponents = this@BComponentsConfigBuilder.useComponents
        override val componentFilters = this@BComponentsConfigBuilder.componentFilters.toImmutableList()
    }
}