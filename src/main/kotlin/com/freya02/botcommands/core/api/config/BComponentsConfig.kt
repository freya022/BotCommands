package com.freya02.botcommands.core.api.config

import com.freya02.botcommands.api.components.ComponentInteractionFilter
import com.freya02.botcommands.api.components.ComponentManager
import com.freya02.botcommands.api.components.DefaultComponentManager
import com.freya02.botcommands.internal.LockableVar
import com.freya02.botcommands.internal.lockableNotNull
import com.freya02.botcommands.internal.toDelegate
import kotlin.properties.Delegates

class BComponentsConfig internal constructor(config: BConfig) {
    /**
     * Sets the type of the service to use as a component manager
     * Used to handle storing/retrieving persistent/lambda components handlers
     *
     * @see DefaultComponentManager
     */
    var componentManagerStrategy: Class<out ComponentManager> by Delegates.lockableNotNull(config, "Component manager needs to be set !")
    fun hasComponentManagerStrategy() = ::componentManagerStrategy.toDelegate<LockableVar<*>>().hasValue()

    private val componentFilters: MutableList<ComponentInteractionFilter> = arrayListOf()

    /**
     * Adds a filter for the component interaction listener, this will check all components such as buttons and selection menus
     *
     * If one of the filters returns `false`, then the component's code is not executed
     *
     * **You still have to acknowledge to the interaction !**
     *
     * @param filter The filter to add
     *
     * @see removeComponentFilter
     */
    fun addComponentFilter(filter: ComponentInteractionFilter) {

    }

    /**
     * Removes a previously set component interaction filter
     *
     * @param filter The filter to remove
     *
     * @see addComponentFilter
     */
    fun removeComponentFilter(filter: ComponentInteractionFilter) {

    }
}