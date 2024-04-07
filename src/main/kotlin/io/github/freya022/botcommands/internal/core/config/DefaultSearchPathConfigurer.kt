package io.github.freya022.botcommands.internal.core.config

import io.github.freya022.botcommands.api.core.config.BConfigBuilder
import io.github.freya022.botcommands.api.core.config.BConfigConfigurer
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Configuration

private val logger = KotlinLogging.logger { }

@Configuration
internal class DefaultSearchPathConfigurer(private val applicationContext: ApplicationContext) : BConfigConfigurer {
    override fun configure(builder: BConfigBuilder) {
        // Spring does funny stuff by including inner classes that extends classes outside the classpath
        // kotlin-reflect doesn't like getting functions when there is such inner class
        // see org.springframework.context.support.DefaultLifecycleProcessor.CracResourceAdapter
        val disabledPackages = setOf("org.springframework")
        val allBeans = applicationContext.beanDefinitionNames.mapNotNullTo(hashSetOf()) { name ->
            val type = applicationContext.getType(name)
            if (type == null) {
                logger.warn { "Could not determine type of service '$name', it will not be searched for annotations" }
                return@mapNotNullTo null
            }
            if (disabledPackages.any { type.packageName.startsWith(it) })
                return@mapNotNullTo null
            type
        }
        allBeans.forEach(builder::addClass)
    }
}