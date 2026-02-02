package io.github.freya022.botcommands.internal.core.service

import io.github.classgraph.ClassInfo
import io.github.classgraph.MethodInfo
import io.github.freya022.botcommands.api.BCInfo
import io.github.freya022.botcommands.api.core.config.BConfig
import io.github.freya022.botcommands.api.core.config.BServiceConfig
import io.github.freya022.botcommands.api.core.service.BCServiceContainer
import io.github.freya022.botcommands.api.core.service.ClassGraphProcessor
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.service.putServiceWithTypeAlias
import io.github.freya022.botcommands.internal.core.Version
import io.github.freya022.botcommands.internal.core.service.provider.ServiceProviders
import net.dv8tion.jda.api.JDAInfo

private const val SERVICE_ANNOTATION_NAME = "io.github.freya022.botcommands.api.core.service.annotations.BService"

internal class BCBotCommandsBootstrap internal constructor(
    config: BConfig
) : AbstractBotCommandsBootstrap(config) {
    internal val serviceConfig: BServiceConfig get() = config.serviceConfig

    internal val serviceProviders = ServiceProviders()
    override val serviceContainer = BCServiceContainerImpl(this)
    override val classGraphProcessors: Set<ClassGraphProcessor> =
        setOf(ConditionalObjectChecker(), serviceProviders)

    init {
        logger.debug { "Loading BotCommands ${BCInfo.VERSION} (${BCInfo.BUILD_TIME}) ; Compiled with JDA ${BCInfo.BUILD_JDA_VERSION} ; Running with JDA ${JDAInfo.VERSION}" }
        Version.checkVersions()

        init()
    }

    internal fun injectAndLoadServices() = measure("Created services") {
        serviceContainer.putServiceWithTypeAlias<BotCommandsBootstrap>(this)

        serviceContainer.putService(serviceContainer, serviceContainer::class, typeAliases = setOf(BCServiceContainer::class, ServiceContainer::class))
        serviceContainer.putService(serviceProviders)

        config.configs.forEach { serviceContainer.putServiceAs(it, it.configType) }

        serviceContainer.loadServices()
    }

    override fun isService(classInfo: ClassInfo): Boolean {
        return classInfo.hasAnnotation(SERVICE_ANNOTATION_NAME)
    }

    override fun isServiceFactory(methodInfo: MethodInfo): Boolean {
        return methodInfo.hasAnnotation(SERVICE_ANNOTATION_NAME)
    }
}
