package io.github.freya022.botcommands.internal.core

import dev.minn.jda.ktx.events.CoroutineEventManager
import io.github.freya022.botcommands.api.core.ICoroutineEventManagerSupplier
import io.github.freya022.botcommands.api.core.service.annotations.BConfiguration
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.ServiceType
import net.dv8tion.jda.api.hooks.IEventManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@BConfiguration
class CoroutineEventManagerProvider {
    @BService
    @Bean
    @ServiceType(IEventManager::class)
    fun coroutineEventManager(supplier: ICoroutineEventManagerSupplier): CoroutineEventManager =
        supplier.get()
}