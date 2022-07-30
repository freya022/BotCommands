package com.freya02.botcommands.core.internal.data

import com.freya02.botcommands.core.api.annotations.BEventListener
import com.freya02.botcommands.core.api.annotations.BService
import com.freya02.botcommands.core.api.config.BConfig
import com.freya02.botcommands.core.api.events.PreloadServiceEvent
import com.freya02.botcommands.core.internal.ServiceContainer
import com.freya02.botcommands.internal.runInitialization

@BService
internal class DataStoreStartupListener {
    @BEventListener
    fun onPreloadService(event: PreloadServiceEvent, config: BConfig, serviceContainer: ServiceContainer) = runInitialization {
        if (config.hasConnectionProvider()) {
            serviceContainer.putServiceAs(serviceContainer.getService(DataStoreService::class, useNonClasspath = true))
        }
    }
}