package io.github.freya022.botcommands.internal.core.service

import io.github.freya022.botcommands.api.BCInfo
import io.github.freya022.botcommands.api.core.config.*
import io.github.freya022.botcommands.api.core.service.ClassGraphProcessor
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.service.putServiceAs
import io.github.freya022.botcommands.api.core.service.putServiceWithTypeAlias
import io.github.freya022.botcommands.internal.core.Version
import io.github.freya022.botcommands.internal.core.service.condition.CustomConditionsContainer
import io.github.freya022.botcommands.internal.core.service.provider.ServiceProviders
import io.github.freya022.botcommands.internal.utils.classRef
import io.github.freya022.botcommands.internal.utils.throwInternal
import net.dv8tion.jda.api.JDAInfo

internal class DefaultBotCommandsBootstrap internal constructor(
    config: BConfig
) : AbstractBotCommandsBootstrap(config) {
    internal val serviceConfig: BServiceConfig get() = config.serviceConfig

    private var _stagingClassAnnotations: StagingClassAnnotations? = StagingClassAnnotations(serviceConfig)
    internal val stagingClassAnnotations: StagingClassAnnotations
        get() = _stagingClassAnnotations
            ?: throwInternal("Cannot use ${classRef<StagingClassAnnotations>()} after it has been cleared")
    internal val serviceProviders = ServiceProviders()
    override val serviceContainer = DefaultServiceContainerImpl(this)
    internal val customConditionsContainer = CustomConditionsContainer()
    override val classGraphProcessors: Set<ClassGraphProcessor> =
        setOf(ConditionalObjectChecker, serviceProviders, customConditionsContainer, stagingClassAnnotations.processor)

    init {
        logger.debug { "Loading BotCommands ${BCInfo.VERSION} (${BCInfo.BUILD_TIME}) ; Compiled with JDA ${BCInfo.BUILD_JDA_VERSION} ; Running with JDA ${JDAInfo.VERSION}" }
        Version.checkVersions()

        init()
    }

    internal fun injectAndLoadServices() = measure("Created services") {
        serviceContainer.putServiceWithTypeAlias<BotCommandsBootstrap>(this)

        serviceContainer.putServiceWithTypeAlias<ServiceContainer>(serviceContainer)
        serviceContainer.putService(serviceProviders)

        serviceContainer.putServiceAs<BConfig>(config)
        serviceContainer.putServiceAs<BServiceConfig>(config.serviceConfig)
        serviceContainer.putServiceAs<BDatabaseConfig>(config.databaseConfig)
        serviceContainer.putServiceAs<BLocalizationConfig>(config.localizationConfig)
        serviceContainer.putServiceAs<BApplicationConfig>(config.applicationConfig)
        serviceContainer.putServiceAs<BComponentsConfig>(config.componentsConfig)
        serviceContainer.putServiceAs<BCoroutineScopesConfig>(config.coroutineScopesConfig)
        @Suppress("DEPRECATION")
        serviceContainer.putServiceAs<BDebugConfig>(config.debugConfig)
        serviceContainer.putServiceAs<BTextConfig>(config.textConfig)

        serviceContainer.loadServices()
    }

    internal fun clearStagingAnnotationsMap() {
        _stagingClassAnnotations = null
    }
}