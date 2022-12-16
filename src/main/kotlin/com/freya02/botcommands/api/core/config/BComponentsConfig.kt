package com.freya02.botcommands.api.core.config

import com.freya02.botcommands.api.components.ComponentInteractionFilter
import com.freya02.botcommands.api.core.annotations.InjectedService

@InjectedService
class BComponentsConfig internal constructor(config: BConfig) {
    var useComponents: Boolean = false

    /**
     * Filters for the component interaction listener, they will check all components such as buttons and selection menus
     *
     * If one of the filters returns `false`, then the component's code is not executed
     *
     * **You still have to acknowledge to the interaction !**
     */
    val componentFilters: MutableList<ComponentInteractionFilter> = arrayListOf()
}