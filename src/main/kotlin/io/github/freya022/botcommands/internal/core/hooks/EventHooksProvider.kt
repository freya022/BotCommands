package io.github.freya022.botcommands.internal.core.hooks

import dev.minn.jda.ktx.events.CoroutineEventManager
import io.github.freya022.botcommands.api.core.ICoroutineEventManagerSupplier
import io.github.freya022.botcommands.api.core.JDAService
import io.github.freya022.botcommands.api.core.config.BConfig
import io.github.freya022.botcommands.api.core.config.BCoroutineScopesConfig
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.ServiceType
import io.github.freya022.botcommands.internal.core.service.FunctionAnnotationsMap
import net.dv8tion.jda.api.hooks.IEventManager
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component

@BService
@Component
internal open class EventHooksProvider(
    eventManagerSupplier: ICoroutineEventManagerSupplier,
) {

    private val originalCoroutineEventManager = eventManagerSupplier.get()

    @Bean
    @BService
    @ServiceType(IEventManager::class)
    internal open fun coroutineEventManager(
        eventDispatcher: EventDispatcherImpl,
    ): CoroutineEventManager {
        return DispatcherAwareCoroutineEventManager(originalCoroutineEventManager, eventDispatcher)
    }

    @Bean
    @BService
    fun eventDispatcher(
        coroutineScopesConfig: BCoroutineScopesConfig,
        eventListenerRegistry: EventListenerRegistry,
    ): EventDispatcherImpl {
        return EventDispatcherImpl(coroutineScopesConfig, originalCoroutineEventManager, eventListenerRegistry)
    }

    @Bean
    @BService
    fun eventListenerRegistry(
        config: BConfig,
        serviceContainer: ServiceContainer,
        eventTreeService: EventTreeService,
        jdaService: JDAService,
        functionAnnotationsMap: FunctionAnnotationsMap,
    ): EventListenerRegistry {
        return EventListenerRegistry(config, serviceContainer, originalCoroutineEventManager, eventTreeService, jdaService, functionAnnotationsMap)
    }
}