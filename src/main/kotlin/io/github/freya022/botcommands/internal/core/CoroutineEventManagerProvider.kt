package io.github.freya022.botcommands.internal.core

import dev.minn.jda.ktx.events.CoroutineEventManager
import io.github.freya022.botcommands.api.core.ICoroutineEventManagerSupplier
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.ServiceType
import net.dv8tion.jda.api.hooks.IEventManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@BService // Cannot be a BConfiguration as the function below needs an instance
@Configuration
internal open class CoroutineEventManagerProvider {
    @Bean
    @BService
    @ServiceType(IEventManager::class)
    internal open fun coroutineEventManager(supplier: ICoroutineEventManagerSupplier): CoroutineEventManager =
        supplier.get()
}