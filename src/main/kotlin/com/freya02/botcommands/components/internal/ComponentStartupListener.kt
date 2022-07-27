package com.freya02.botcommands.components.internal

import com.freya02.botcommands.api.components.ComponentManager
import com.freya02.botcommands.core.api.annotations.BEventListener
import com.freya02.botcommands.core.api.annotations.BService
import com.freya02.botcommands.core.api.config.BConfig
import com.freya02.botcommands.core.api.events.PreloadServiceEvent
import com.freya02.botcommands.core.internal.ServiceContainer
import com.freya02.botcommands.internal.runInitialization

@BService
internal class ComponentStartupListener {
    @BEventListener
    fun onPreloadService(event: PreloadServiceEvent, config: BConfig, serviceContainer: ServiceContainer) = runInitialization {
        if (config.hasConnectionProvider() && config.hasComponentManagerStrategy()) {
            @Suppress("RemoveExplicitTypeArguments")
            serviceContainer.putServiceAs<ComponentManager>(serviceContainer.getService(config.componentManagerStrategy, useNonClasspath = true))
        }
    }
}