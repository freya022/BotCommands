package io.github.freya022.botcommands.internal.core.service

import io.github.freya022.botcommands.api.core.config.BConfig
import io.github.freya022.botcommands.api.core.config.BServiceConfig
import io.github.freya022.botcommands.api.core.service.ClassGraphProcessor
import io.github.freya022.botcommands.internal.utils.ReflectionMetadata
import io.github.freya022.botcommands.internal.utils.classRef
import io.github.freya022.botcommands.internal.utils.throwInternal
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.InitializingBean
import org.springframework.stereotype.Component
import kotlin.time.DurationUnit
import kotlin.time.measureTime

private val logger = KotlinLogging.logger { }

@Component
internal class SpringServiceBootstrap internal constructor(
        private val config: BConfig,
        serviceConfig: BServiceConfig,
        override val serviceContainer: SpringServiceContainer
) : ServiceBootstrap, InitializingBean {
    private var _stagingClassAnnotations: StagingClassAnnotations? = StagingClassAnnotations(serviceConfig)
    final override val stagingClassAnnotations: StagingClassAnnotations
        get() = _stagingClassAnnotations
                ?: throwInternal("Cannot use ${classRef<StagingClassAnnotations>()} after it has been clearer")
    final override val classGraphProcessors: Set<ClassGraphProcessor> = setOf(stagingClassAnnotations.processor)

    override fun afterPropertiesSet() {
        measureTime {
            ReflectionMetadata.runScan(config, this)
        }.also { logger.debug { "Reflection metadata took ${it.toString(DurationUnit.MILLISECONDS, 2)}" } }
    }

    final override fun clearStagingAnnotationsMap() {
        _stagingClassAnnotations = null
    }
}