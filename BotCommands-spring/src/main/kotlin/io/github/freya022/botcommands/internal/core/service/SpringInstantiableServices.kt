package io.github.freya022.botcommands.internal.core.service

import io.github.freya022.botcommands.api.core.config.BConfig
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

private val logger = KotlinLogging.logger { }

@Service
internal class SpringInstantiableServices internal constructor(
    private val config: BConfig,
    private val applicationContext: ApplicationContext
) : InstantiableServices {
    override fun getAllPrimaryTypes(): Set<KClass<*>> {
        return applicationContext.beanDefinitionNames
            .asSequence()
            .map { beanName ->
                val type = applicationContext.getType(beanName)
                if (type != null) {
                    type
                } else {
                    logger.debug { "Creating bean '$beanName' as no type is available" }
                    applicationContext.getBean(beanName).javaClass
                }
            }
            .filter { type ->
                if (config.packages.any { type.packageName.startsWith(it) })
                    return@filter true
                if (type.packageName.startsWith("io.github.freya022.botcommands"))
                    return@filter true
                if (type in config.classes)
                    return@filter true
                return@filter false
            }
            .mapTo(hashSetOf()) { it.kotlin }
    }
}