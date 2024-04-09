package io.github.freya022.botcommands.internal.core.service

import io.github.freya022.botcommands.api.core.config.*
import io.github.freya022.botcommands.api.core.service.ClassGraphProcessor
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.service.putServiceAs
import io.github.freya022.botcommands.api.core.service.putServiceWithTypeAlias
import io.github.freya022.botcommands.internal.core.service.condition.CustomConditionsContainer
import io.github.freya022.botcommands.internal.core.service.provider.ServiceProviders
import io.github.freya022.botcommands.internal.utils.classRef
import io.github.freya022.botcommands.internal.utils.throwInternal

internal class DefaultServiceBootstrap internal constructor(config: BConfig) : ServiceBootstrap {
    internal val serviceConfig: BServiceConfig = config.serviceConfig

    private var _stagingClassAnnotations: StagingClassAnnotations? = StagingClassAnnotations(serviceConfig)
    override val stagingClassAnnotations: StagingClassAnnotations
        get() = _stagingClassAnnotations ?: throwInternal("Cannot use ${classRef<StagingClassAnnotations>()} after it has been clearer")
    internal val serviceProviders = ServiceProviders()
    override val serviceContainer = DefaultServiceContainerImpl(this)
    internal val customConditionsContainer = CustomConditionsContainer()
    override val classGraphProcessors: Set<ClassGraphProcessor> =
        setOf(ConditionalObjectChecker, serviceProviders, customConditionsContainer, stagingClassAnnotations.processor)

    init {
        serviceContainer.putServiceWithTypeAlias<ServiceBootstrap>(this)

        // In the spring bootstrapper, it would be replaced a service loaded by spring
        serviceContainer.putServiceWithTypeAlias<ServiceContainer>(serviceContainer)
        serviceContainer.putService(serviceProviders)

        serviceContainer.putServiceAs<BConfig>(config)
        serviceContainer.putServiceAs<BServiceConfig>(config.serviceConfig)
        serviceContainer.putServiceAs<BDatabaseConfig>(config.databaseConfig)
        serviceContainer.putServiceAs<BApplicationConfig>(config.applicationConfig)
        serviceContainer.putServiceAs<BComponentsConfig>(config.componentsConfig)
        serviceContainer.putServiceAs<BCoroutineScopesConfig>(config.coroutineScopesConfig)
        serviceContainer.putServiceAs<BDebugConfig>(config.debugConfig)
        serviceContainer.putServiceAs<BTextConfig>(config.textConfig)
    }

    override fun clearStagingAnnotationsMap() {
        _stagingClassAnnotations = null
    }
}