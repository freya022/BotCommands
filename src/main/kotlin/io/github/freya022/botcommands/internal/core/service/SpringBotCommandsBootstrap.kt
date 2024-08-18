package io.github.freya022.botcommands.internal.core.service

import io.github.classgraph.ClassInfo
import io.github.classgraph.MethodInfo
import io.github.freya022.botcommands.api.BCInfo
import io.github.freya022.botcommands.api.core.config.BConfig
import io.github.freya022.botcommands.api.core.service.ClassGraphProcessor
import io.github.freya022.botcommands.internal.core.Version
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.JDAInfo
import org.springframework.boot.context.event.ApplicationContextInitializedEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger { }

internal object SpringBotCommandsVersionChecker : ApplicationListener<ApplicationContextInitializedEvent> {
    // To run before anything else
    // https://stackoverflow.com/a/58870796
    override fun onApplicationEvent(event: ApplicationContextInitializedEvent) {
        logger.debug { "Running with BotCommands ${BCInfo.VERSION} (${BCInfo.BUILD_TIME}) ; Compiled with JDA ${BCInfo.BUILD_JDA_VERSION} ; Running with JDA ${JDAInfo.VERSION}" }
        Version.checkVersions()
    }
}

private const val COMPONENT_ANNOTATION_NAME = "org.springframework.stereotype.Component"
private const val BEAN_ANNOTATION_NAME = "org.springframework.context.annotation.Bean"

@Component
internal class SpringBotCommandsBootstrap internal constructor(
    config: BConfig,
    override val serviceContainer: SpringServiceContainer
) : AbstractBotCommandsBootstrap(config) {
    override val classGraphProcessors: Set<ClassGraphProcessor> = emptySet()

    init {
        init()
    }

    override fun isService(classInfo: ClassInfo): Boolean {
        return classInfo.annotations.containsName(COMPONENT_ANNOTATION_NAME)
    }

    override fun isServiceFactory(methodInfo: MethodInfo): Boolean {
        return methodInfo.annotationInfo.containsName(BEAN_ANNOTATION_NAME)
    }
}