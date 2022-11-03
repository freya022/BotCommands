package com.freya02.botcommands.api.core.config

import com.freya02.botcommands.api.components.ComponentInteractionFilter
import com.freya02.botcommands.api.components.ComponentManager
import com.freya02.botcommands.api.components.DefaultComponentManager
import com.freya02.botcommands.api.core.annotations.LateService
import com.freya02.botcommands.internal.LockableVar
import com.freya02.botcommands.internal.lockableNotNull
import com.freya02.botcommands.internal.toDelegate
import kotlin.properties.Delegates

@LateService
class BComponentsConfig internal constructor(config: BConfig) {
    /**
     * Sets the type of the service to use as a component manager
     * Used to handle storing/retrieving persistent/lambda components handlers
     *
     * @see DefaultComponentManager
     */
    var componentManagerStrategy: Class<out ComponentManager> by Delegates.lockableNotNull(config, "Component manager needs to be set !")
    fun hasComponentManagerStrategy() = ::componentManagerStrategy.toDelegate<LockableVar<*>>().hasValue()

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