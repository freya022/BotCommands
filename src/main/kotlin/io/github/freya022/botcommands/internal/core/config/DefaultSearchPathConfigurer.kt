package io.github.freya022.botcommands.internal.core.config

import io.github.freya022.botcommands.api.core.annotations.EnableBotCommands
import io.github.freya022.botcommands.api.core.config.BConfigBuilder
import io.github.freya022.botcommands.api.core.config.BConfigConfigurer
import io.github.freya022.botcommands.internal.utils.annotationRef
import org.springframework.beans.factory.getBeansWithAnnotation
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.ComponentScans
import org.springframework.context.annotation.Configuration

@Configuration
internal class DefaultSearchPathConfigurer(private val applicationContext: ApplicationContext) : BConfigConfigurer {
    override fun configure(builder: BConfigBuilder) {
        val beans = applicationContext.getBeansWithAnnotation<EnableBotCommands>()
        check(beans.size == 1) {
            "Cannot have multiple classes with ${annotationRef<EnableBotCommands>()}"
        }

        val beanName = beans.keys.single()
        val scans = applicationContext.findAllAnnotationsOnBean(beanName, ComponentScan::class.java, true)
        val groupScans = applicationContext.findAllAnnotationsOnBean(beanName, ComponentScans::class.java, true).flatMap { it.value.asIterable() }

        (scans + groupScans).forEach { builder.packages += it.packages }
    }

    private val ComponentScan.packages: Set<String>
        get() = buildSet(basePackages.size + basePackageClasses.size) {
            this += basePackages
            basePackageClasses.forEach { this += it.java.packageName }
        }
}