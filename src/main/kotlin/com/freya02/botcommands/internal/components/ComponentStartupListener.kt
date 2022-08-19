package com.freya02.botcommands.internal.components

import com.freya02.botcommands.api.components.ComponentManager
import com.freya02.botcommands.api.core.annotations.BEventListener
import com.freya02.botcommands.api.core.annotations.BService
import com.freya02.botcommands.api.core.config.BConfig
import com.freya02.botcommands.api.core.events.PreloadServiceEvent
import com.freya02.botcommands.internal.core.ServiceContainer
import com.freya02.botcommands.internal.runInitialization

@BService
internal class ComponentStartupListener {
    @BEventListener
    fun onPreloadService(event: PreloadServiceEvent, config: BConfig, serviceContainer: ServiceContainer) = runInitialization {
        if (config.componentsConfig.hasComponentManagerStrategy()) {
            @Suppress("RemoveExplicitTypeArguments")
            serviceContainer.putServiceAs<ComponentManager>(
                serviceContainer.getService(
                    config.componentsConfig.componentManagerStrategy,
                    useNonClasspath = true
                )
            )
        }
    }
}