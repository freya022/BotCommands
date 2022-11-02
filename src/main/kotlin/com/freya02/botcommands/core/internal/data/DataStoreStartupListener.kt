package com.freya02.botcommands.core.internal.data

import com.freya02.botcommands.api.core.annotations.BEventListener
import com.freya02.botcommands.api.core.annotations.BService
import com.freya02.botcommands.api.core.config.BConfig
import com.freya02.botcommands.api.core.events.PreloadServiceEvent
import com.freya02.botcommands.internal.core.ServiceContainer
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